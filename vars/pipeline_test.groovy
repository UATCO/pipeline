def build(product, tests_path) {

    products = [
    'big_geek_tests': 'https://github.com/UATCO/big_geek_tests',
    ]

    node {
    stage('project_update')
        dir ('C:/ProgramData/Jenkins/projects') {
        def product_branch = params.PRODUCT_BRANCH
        echo ">>>>>>> PRODUCT_BRANCH: ${product_branch}"
        checkout([
          $class: "GitSCM",
          branches: [[name: product_branch]],
          extensions: [[
                $class: 'RelativeTargetDirectory',
                relativeTargetDir: products[product].split('/')[-1]
                ]],
          userRemoteConfigs: [[url: products[product]]]
        ])
      }

    stage('uatf_update')
        dir ('C:/ProgramData/Jenkins/environment') {
        def uatf_branch = params.UATF_BRANCH
        echo ">>>>>>> UATF_BRANCH: ${uatf_branch}"
        checkout([
          $class: "GitSCM",
          branches: [[name: uatf_branch]],
          extensions: [[
                $class: 'RelativeTargetDirectory',
                relativeTargetDir: "uatf"
                ]],
          userRemoteConfigs: [[url: "https://github.com/UATCO/uatf.git"]]
        ])
       }

    stage('test_preparation') {
        def command_copy = '''
        chcp 65001
        xcopy C:\\ProgramData\\Jenkins\\projects\\${product}\\${tests_path} C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\test_job /e /y
        '''
        bat command_copy
    }

    stage('start_tests')
        bat(
        script: '''
        chcp 65001

        set PYTHONPATH=C:\\ProgramData\\Jenkins\\environment\\uatf;C:\\ProgramData\\Jenkins\\projects\\big_geek_tests
        C:\\python311\\python.exe -c "from uatf.run import RunTests;RunTests().run_tests()"
        '''
        )
    archiveArtifacts "artifacts.zip"
    junit keepLongStdio: true, skipOldReports: true, testResults: 'test-reports/*.xml'
}}