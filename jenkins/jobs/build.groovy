pipelineJob("CiAntBuild") {
   description("Builds and tests ci.ant against WLP. Depends on CiAntDependencyBuild having run first.")
   keepDependencies(false)
   definition {
      cpsScm {
         scm {
            git("https://github.com/OpenLiberty/ci.ant.git", "main")
         }
         scriptPath("jenkins/build.jenkinsfile")
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
