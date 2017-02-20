package net.wasdev.wlp.ant.jsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;

import net.wasdev.wlp.ant.ServerTask;

public class CompileJSPs extends Task {

    private File wlpHome;
    private File war;
    private String ref;
    private List<String> features = new ArrayList<>();
    private String featureVersion = "2.3";

    // By default delete temporary server afterwards
    private boolean cleanup = true;

    private File srcdir;
    private File tmpdir;
    private File destdir;
    private String classpath = "";
    private String classpathRef;
    private String source;

    @Override
    public void execute() {
        validate();

        try {
            // Need somewhere to compile to so create a tmp file and turn it to
            // a directory
            File compileDir;
            if (tmpdir == null) {
                compileDir = File.createTempFile("compileJsp", "");
                compileDir.delete();
            } else {
                compileDir = new File(tmpdir, "compileJsp");
            }

            try {
                File serverDir = new File(compileDir, "servers/defaultServer/");
                String warSuffix = (war == null) ? "fake" : trimExtension(war.getName());
                File jspCompileDir = new File(serverDir, "jsps/default_node/SMF_WebContainer/jspCompile/" + warSuffix);
                if (jspCompileDir.exists()) {
                    delete(jspCompileDir);
                }
                if (serverDir.exists() || serverDir.mkdirs()) {
                    writeServerXML(serverDir);
                    createAppXML(serverDir);
                    // Compile jsps by having the server start with eager app
                    // start and compilation
                    ServerTask server = createServerTask(compileDir);
                    server.setOperation("start");
                    server.execute();

                    boolean compileSuccess = false;
                    try {
                        checkFeaturesExist(serverDir);

                        compileSuccess = waitForCompilation(serverDir, jspCompileDir, war);
                    } finally {
                        // Stop the server
                        server.setOperation("stop");
                        server.execute();
                    }
                    if (compileSuccess) {
                        if (war != null) {
                            updateSourceWar(jspCompileDir);
                        } else {
                            copyClassFiles(jspCompileDir);
                        }
                    } else {
                        printCompileErrors(new File(serverDir, "logs/console.log"));
                        throw new BuildException("JSP compile failed");
                    }
                } else {
                    throw new BuildException("Unable to create folder for usr dir");
                }
            } finally {
                if (cleanup) {
                    delete(compileDir);
                }
            }
        } catch (IOException e) {
            throw new BuildException("A failure occurred: " + e.toString(), e);
        }

    }

    private void copyClassFiles(File jspCompileDir) {
        Copy copy = new Copy();
        copy.setProject(getProject());
        copy.setTaskName(getTaskName());
        destdir.mkdirs();
        copy.setTodir(destdir);
        FileSet files = new FileSet();
        files.setDir(jspCompileDir);
        files.setIncludes("**/*.class");
        copy.addFileset(files);
        copy.execute();
    }

    private void updateSourceWar(File jspCompileDir) {
        // Finally need to merge the compiled jsps in
        War warTask = new War();
        warTask.setProject(getProject());
        warTask.setDestFile(war);
        warTask.setUpdate(true);
        ZipFileSet jspFiles = new ZipFileSet();
        // The JSPs will be in the a well known location. The
        // app name from server.xml and the war file name will
        // be
        // in the path, the war name minus the .war extension
        // (if present) will also be used.
        jspFiles.setDir(jspCompileDir);
        warTask.addClasses(jspFiles);
        warTask.setTaskName(getTaskName());
        warTask.execute();
    }

    private void validate() {
        if (war == null && srcdir == null) {
            throw new BuildException("One of war or srcdir must be specified");
        }

        if (srcdir != null && destdir == null) {
            throw new BuildException("The destdir must be specified");
        }

        if (wlpHome == null) {
            throw new BuildException("Liberty installation directory must be set");
        }

        if (source == null) {
            setSource(System.getProperty("java.specification.version"));
        }
    }

    private void checkFeaturesExist(File serverDir) {
        BufferedReader reader = null;
        boolean fail = false;
        try {
            reader = new BufferedReader(new FileReader(new File(serverDir, "logs/console.log")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("CWWKF0001E")) {
                    log(line);
                    fail = true;
                }
            }
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                }
            }
        }

        if (fail) {
            throw new BuildException("Features required to compile are missing");
        }
    }

    private void printCompileErrors(File log) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(log));
            try {
                String line;
                boolean reprint = false;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("JSPG0049E") || line.startsWith("JSPG0091E") || line.startsWith("JSPG0093E")) {
                        reprint = true;
                    }
                    if (line.startsWith("[")) {
                        reprint = false;
                    }

                    if (reprint) {
                        log(line, Project.MSG_ERR);
                    }
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe) {
            throw new BuildException("Unable to load compile log");
        }
    }

    private boolean waitForCompilation(File serverDir, File jspCompileDir, File war2) throws IOException {
        List<File> jsps = new ArrayList<>();

        if (war2 != null) {
            fillFromWar(jsps, war2, jspCompileDir);
        } else {
            fillFromSource(jsps, srcdir, jspCompileDir);
        }
        Set<File> javaFiles = new HashSet<>();

        boolean equalDetected = false;
        // Only check 30 times so we aren't waiting stupidly long. This
        // might be bad for really big apps, but an escape is needed
        // in case something goes horribly wrong.
        for (int i = 0; i < 30; i++) {
            if (jspCompileDir.exists()) {
                // Look to see if the class file exists yet.
                Iterator<File> it = jsps.iterator();
                while (it.hasNext()) {
                    File classFile = it.next();
                    if (classFile.exists()) {
                        it.remove();
                        equalDetected = false;
                    }
                    String classFileName = classFile.getName();
                    String javaFileName = classFileName.substring(0, classFileName.length() - 6) + ".java";
                    File javaFile = new File(classFile.getParentFile(), javaFileName);
                    // If the class file doesn't exist yet look to see if a
                    // .java file exists which might indicate
                    // a compile failure and store it away.
                    if (javaFile.exists()) {
                        if (javaFiles.add(javaFile)) {
                            equalDetected = false;
                        }
                    } else {
                        if (javaFiles.remove(javaFile)) {
                            equalDetected = false;
                        }
                    }
                }
            }
            if (!!!equalDetected) {
                equalDetected = jsps.size() == javaFiles.size();
                if (jsps.isEmpty()) {
                    break;
                }
                try {
                    // Wait for a second for compilation to progress
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }

        if (!jsps.isEmpty()) {
            log("Failed to create: " + jsps, Project.MSG_ERR);
        }

        return jsps.isEmpty();

    }

    private void fillFromSource(List<File> jsps, File srcdir2, File jspCompileDir) {
        File[] files = srcdir2.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(".jsp")) {
                    String expectedJSPName = jspToClassFileName(fileName);
                    jsps.add(new File(jspCompileDir, expectedJSPName));
                } else if (file.isDirectory()) {
                    fillFromSource(jsps, file, new File(jspCompileDir, file.getName()));
                }
            }
        }
    }

    private String jspToClassFileName(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append('_');
        char[] chars = name.toCharArray();
        for (int i = 0; i < name.length() - 4; i++) {
            char c = chars[i];
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append('_');
                sb.append(Integer.toHexString(c).toUpperCase());
                sb.append('_');
            }
        }

        sb.append(".class");

        return sb.toString();
    }

    private void fillFromWar(List<File> jsps, File war2, File jspCompileDir) {
        ZipInputStream zipIn = null;
        try {
            zipIn = new ZipInputStream(new FileInputStream(war2));

            // Find all the jsps in the war file and work out where the compiled
            // result should be
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.endsWith(".jsp")) {
                    String expectedJSPName = jspToClassFileName(entryName);
                    jsps.add(new File(jspCompileDir, expectedJSPName));
                }
            }
        } catch (IOException ioe) {
        } finally {
            if (zipIn != null) {
                try {
                    zipIn.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    private ServerTask createServerTask(File usrDir) {
        ServerTask server = new ServerTask();
        server.setProject(getProject());
        server.setRef(ref);
        server.setInstallDir(wlpHome);
        server.setUserDir(usrDir);
        server.setTaskName(getTaskName());
        return server;
    }

    private void writeServerXML(File serverDir) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new File(serverDir, "server.xml"));
        ps.println("<server>");
        ps.println("<featureManager>");
        for (String feature : features) {
            ps.print("<feature>");
            ps.print(feature);
            ps.println("</feature>");
        }
        ps.print("<feature>jsp-");
        ps.print(featureVersion);
        ps.println("</feature>");
        ps.println("</featureManager>");
        if (war != null) {
            ps.println("<webApplication name=\"jspCompile\" location=\"" + war.getAbsolutePath() + "\"/>");
        } else {
            ps.println("<webApplication name=\"jspCompile\" location=\"fake.war\"/>");
        }
        ps.println("<httpEndpoint id=\"defaultHttpEndpoint\" host=\"localhost\" httpPort=\"0\"/>");
        ps.print("<jspEngine prepareJsps=\"0\" scratchdir=\"" + serverDir.getAbsolutePath() + "/jsps\" jdkSourceLevel=\"" + source + "\"/>");
        ps.println("<webContainer deferServletLoad=\"false\"/>");
        ps.println("<keyStore password=\"dummyKeystore\"/>");
        ps.println("</server>");
        ps.close();
    }

    private void createAppXML(File serverDir) throws FileNotFoundException {
        if (srcdir != null) {
            // TODO write the loose application xml.
            File appsDir = new File(serverDir, "apps");
            appsDir.mkdirs();
            PrintStream ps = new PrintStream(new File(appsDir, "fake.war.xml"));
            ps.println("<archive>");
            ps.println("  <dir targetInArchive=\"/\" sourceOnDisk=\"" + srcdir.getAbsolutePath() + "\"/>");

            Path p = new Path(getProject(), classpath);
            if (classpathRef != null) {
                Path path = (Path) getProject().getReference(classpathRef);
                p.add(path);
            }
            String[] cp = p.toString().split(File.pathSeparator);
            for (String entry : cp) {
                File f = new File(entry);
                String basename = f.getName();
                if (f.isFile() && f.exists() && f.getName().endsWith(".jar")) {
                    ps.println("  <file targetInArchive=\"/WEB-INF/lib/" + basename + "\" sourceOnDisk=\"" + f.getAbsolutePath() + "\"/>");
                } else if (f.isDirectory() && f.exists()) {
                    // TODO: What if basename is NOT "classes"?
                    ps.println("  <dir targetInArchive=\"/WEB-INF/classes\" sourceOnDisk=\"" + f.getAbsolutePath() + "\"/>");
                }
            }

            ps.println("</archive>");
            ps.close();
        }
    }

    private String trimExtension(String name) {
        if (name.endsWith(".war")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    private void delete(File f) {
        if (f.isFile()) {
            f.delete();
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
            f.delete();
        }
    }

    public void setCleanup(boolean cleanup) {
        this.cleanup = cleanup;
    }

    public void setInstallDir(File home) {
        wlpHome = home;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setWar(File war) {
        this.war = war;
    }

    public void setFeatures(String features) {
        String[] featuresArray = features.split(",");
        this.features.addAll(Arrays.asList(featuresArray));
    }

    public void setJspVersion(String version) {
        featureVersion = version;
    }

    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setTempdir(File tmpdir) {
        this.tmpdir = tmpdir;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public void setClasspathRef(String classpathRef) {
        this.classpathRef = classpathRef;
    }

    public void setSource(String src) {
        if ("1.3".equals(src)) {
            source = "13";
        } else if ("1.4".equals(src)) {
            source = "14";
        } else if ("1.5".equals(src) || "5".equals(src)) {
            source = "15";
        } else if ("1.6".equals(src) || "6".equals(src)) {
            source = "16";
        } else if ("1.7".equals(src) || "7".equals(src)) {
            source = "17";
        } else if ("1.8".equals(src) || "8".equals(src)) {
            source = "18";
        }
    }
}
