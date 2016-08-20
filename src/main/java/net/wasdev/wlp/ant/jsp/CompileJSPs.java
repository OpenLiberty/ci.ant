package net.wasdev.wlp.ant.jsp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.ZipFileSet;

import net.wasdev.wlp.ant.ServerTask;

public class CompileJSPs extends Task {

  private File wlpHome;
  private File war;
  private String ref;
  private List<String> features = new ArrayList<>();
  private String featureVersion = "2.3";
  
  public void execute() {
    try {
      // Need somewhere to compile to so create a tmp file and turn it to a directory
      File compileDir = File.createTempFile("precompileJsp", "");
      compileDir.delete();
      try {
        File serverDir = new File(compileDir, "servers/defaultServer/");
        File jspCompileDir = new File(serverDir, "jsps/default_node/SMF_WebContainer/jspCompile/" + trimExtension(war.getName()));
        if (serverDir.mkdirs()) {
          writeServerXML(serverDir, war);
          // Compile jsps by having the server start with eager app start and compilation
          ServerTask server = createServerTask(compileDir);
          server.setOperation("start");
          server.execute();

          checkFeaturesExist(serverDir);

          boolean compileSuccess = false;
          try {
              compileSuccess = waitForCompilation(serverDir, jspCompileDir, war);
          } finally {
              // Stop the server
              server.setOperation("stop");
              server.execute();
          }
          if (compileSuccess) {
              // Finally need to merge the compiled jsps in
              War warTask = new War();
              warTask.setProject(getProject());
              warTask.setDestFile(war);
              warTask.setUpdate(true);
              ZipFileSet jspFiles = new ZipFileSet();
              // The JSPs will be in the a well known location. The app name from server.xml and the war file name will be
              // in the path, the war name minus the .war extension (if present) will also be used.
              jspFiles.setDir(jspCompileDir);
              // The JSPs are in the package com.ibm._jsp, but they are not compiled into that structure, so make sure it goes
              // into the war with that prefix. Note we are calling addZipfileset here because using addClasses on the War task
              // doesn't use the prefix, so instead we set a zip fileset and specify the full path into the war.
              jspFiles.setPrefix("WEB-INF/classes/com/ibm/_jsp/");
              warTask.addZipfileset(jspFiles);
              warTask.setTaskName(getTaskName());
              warTask.execute();
          } else {
              printCompileErrors(new File(serverDir, "logs/console.log"));
              throw new BuildException("JSP compile failed");
          }
        } else {
          throw new BuildException("Unable to create folder for usr dir");
        }
      } finally {
        delete(compileDir);
      }
    } catch (IOException e) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);
            e.printStackTrace(ps);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
            String line;
            while ((line = reader.readLine()) != null) {
                log(line);
            }
        } catch (IOException ioe2) {
            
        }
        throw new BuildException("A failure occurred: " + e.toString(), e);
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
      ZipInputStream zipIn = new ZipInputStream(new FileInputStream(war2));
      
      try {
          // Find all the jsps in the war file and work out where the compiled result should be
          List<File> jsps = new ArrayList<>();
          ZipEntry entry;
          while ((entry = zipIn.getNextEntry()) != null) {
              String entryName = entry.getName();
              if (entryName.endsWith(".jsp")) {
                  String expectedJSPName = '_' + entryName.substring(0, entryName.length() - 4) + ".class";
                  jsps.add(new File(jspCompileDir, expectedJSPName));
              }
          }
          
          Set<File> javaFiles = new HashSet<>();
          
          boolean equalDetected = false;
          // Only check 30 times so we aren't waiting stupidly long. This might be bad for really big apps, but an escape is needed
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
                      String javaFileName = classFileName.substring(0, classFileName.length() - 6);
                      File javaFile = new File(classFile.getParentFile(), javaFileName);
                      // If the class file doesn't exist yet look to see if a .java file exists which might indicate
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
      } finally {
          zipIn.close();
      }
      
      return true;
    
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

  private void writeServerXML(File serverDir, File war2) throws FileNotFoundException {
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
      ps.println("<webApplication name=\"jspCompile\" location=\"" + war.getAbsolutePath() + "\"/>");
      ps.println("<httpEndpoint id=\"defaultHttpEndpoint\" httpPort=\"0\"/>");
      ps.println("<jspEngine prepareJsps=\"0\" scratchdir=\"" + serverDir.getAbsolutePath() + "/jsps\"/>");
      ps.println("<webContainer deferServletLoad=\"false\"/>");
      ps.println("<keyStore password=\"dummyKeystore\"/>");
      ps.println("</server>");
      ps.close();
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
}