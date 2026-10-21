pipelineJob("CiAntDependencyBuild") {
   description("Builds and installs ci.ant as a Maven dependency (mvn clean install -DskipTests). Runs first in the ci.ant PR pipeline.")
   keepDependencies(false)
   definition {
      cpsScm {
         scm {
            git("https://github.com/OpenLiberty/ci.ant.git", "main")
         }
         scriptPath("jenkins/buildDependency.jenkinsfile")
         lightweight()
      }
   }
   disabled(false)
   configure {
      it / 'properties' / 'com.sonyericsson.rebuild.RebuildSettings' {
         'autoRebuild'('false')
         'rebuildDisabled'('false')
      }
   }
}
