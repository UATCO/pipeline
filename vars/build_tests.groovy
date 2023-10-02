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

        def project_tests_path = "C:\\ProgramData\\Jenkins\\projects\\${product}\\${tests_path}"
        def job_tests_path = "C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\${JOB_NAME.replace('/', '\\')}"

        def command_copy = """
        chcp 65001
        xcopy ${project_tests_path}  ${job_tests_path} /e /y
        xcopy ${job_tests_path}\\config\\show-config.ini ${job_tests_path}\\config.ini /e /y
        """
        bat(script: command_copy)
    }

    stage('start_tests')
        def command_start_tests = """
        chcp 65001
        set PYTHONPATH=C:\\ProgramData\\Jenkins\\environment\\uatf;C:\\ProgramData\\Jenkins\\projects\\${product}
        C:\\python311\\python.exe -c "from uatf.run import RunTests;RunTests().run_tests()"
        """

        bat(
        script: command_start_tests
        )

    archiveArtifacts "artifacts.zip"
    junit keepLongStdio: true, skipOldReports: true, testResults: 'test-reports/*.xml'
}}