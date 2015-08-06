/**
 * (C) Copyright IBM Corporation 2014, 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.ant;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * undeploy ant task
 */
public class UndeployTask extends AbstractTask {

    private static final String STOP_APP_MESSAGE_CODE_REG = "CWWKZ0009I.*";
    
    private String fileName;
    private PatternSet pattern;
    private String timeout;
    private static final long APP_STOP_TIMEOUT_DEFAULT = 30 * 1000;

    @Override
    public void execute() {
        super.initTask();

        final List<File> files = scanFileSets();

        long appStopTimeout = APP_STOP_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStopTimeout = Long.valueOf(timeout);
        }
        
        for (File file : files) {
            log(MessageFormat.format(messages.getString("info.undeploy"), file.getName()));
            FileUtils.delete(file);

            //check stop message code
            String stopMessage = STOP_APP_MESSAGE_CODE_REG + getFileName(file.getName());
            if (waitForStringInLog(stopMessage, appStopTimeout, getLogFile()) == null) {
                throw new BuildException(MessageFormat.format(messages.getString("error.undeploy.fail"), file.getPath()));
            }
        }
    }

    private List<File> scanFileSets() throws BuildException {
        File dropinsDir = new File(serverConfigRoot, "dropins");
        final List<File> list = new ArrayList<File>();

        if (fileName != null) {
            File fileUndeploy = new File(dropinsDir, fileName);
            if (fileUndeploy.exists()) {
                list.add(fileUndeploy);
            } else {
                throw new BuildException(MessageFormat.format(messages.getString("error.undeploy.file.noexist"), fileUndeploy.getPath()));
            }
        } else {
            FileSet dropins = new FileSet();
            dropins.setDir(dropinsDir);

            if (pattern != null) {
                dropins.appendIncludes(pattern.getIncludePatterns(getProject()));
                dropins.appendExcludes(pattern.getExcludePatterns(getProject()));
            }

            final DirectoryScanner ds = dropins.getDirectoryScanner(getProject());
            ds.scan();
            final String[] names = ds.getIncludedFiles();

            if (names.length == 0) {
                throw new BuildException(messages.getString("error.undeploy.fileset.invalid"));
            }

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }
        return list;
    }

    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFile() {
        return this.fileName;
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public void addPatternset(PatternSet pattern) {
        this.pattern=pattern;
    }

}
