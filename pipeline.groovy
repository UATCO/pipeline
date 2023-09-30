node {
    stage('project_update')
        dir ('C:/ProgramData/Jenkins/projects') {
        checkout([
          $class: "GitSCM",
          branches: [[name: "debug"]],
          extensions: [[
                $class: 'RelativeTargetDirectory',
                relativeTargetDir: "big_geek_tests"
                ]],
          userRemoteConfigs: [[url: "https://github.com/UATCO/big_geek_tests"]]
        ])
      }

    stage('uatf_upadte')
        dir ('C:/ProgramData/Jenkins/environment') {
        checkout([
          $class: "GitSCM",
          branches: [[name: "jenkins_adoptate"]],
          extensions: [[
                $class: 'RelativeTargetDirectory',
                relativeTargetDir: "uatf"
                ]],
          userRemoteConfigs: [[url: "https://github.com/UATCO/uatf.git"]]
        ])
       }

    stage('test_preparation') {
        bat '''
        chcp 65001
        xcopy C:\\ProgramData\\Jenkins\\projects\\big_geek_tests\\test-auth\\smoke\\test_auth C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\test_job /e /y'''
    }

    stage('start_tests')
        bat(
        script: '''
        chcp 65001

        set PYTHONPATH=C:\\ProgramData\\Jenkins\\environment\\uatf;C:\\ProgramData\\Jenkins\\projects\\big_geek_tests
        C:\\python311\\python.exe -c "from uatf.run import RunTests;RunTests().run_tests()"
        '''
        )
    dir('artifact') {
        archiveArtifacts "report.html"
    }
}