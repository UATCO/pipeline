
def get_product_project(product_name) {

    all_products = [
    'big_geek_tests': 'https://github.com/UATCO/big_geek_tests',
    ]

    return all_products[product]
}

def build(product, tests_path) {

    all_products = [
    'big_geek_tests': 'https://github.com/UATCO/big_geek_tests',
    ]

    product_git = all_products[product]

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
                relativeTargetDir: product_git.split('/')[-1]
                ]],
          userRemoteConfigs: [[url: product_git]]
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
        def job_tests_path = "C:\\ProgramData\\Jenkins\\.jenkins\\workspace\\${JOB_NAME.replace('/', '\\')}\\${tests_path}"

        def command_copy = """
        chcp 65001
        xcopy ${project_tests_path}  ${job_tests_path} /e /y
        copy ${job_tests_path}\\config\\show-config.ini ${job_tests_path}\\config.ini
        """
        bat(script: command_copy)
    }

    stage('start_tests')
        def command_start_tests = """
        chcp 65001
        set PYTHONPATH=C:\\ProgramData\\Jenkins\\environment\\uatf;C:\\ProgramData\\Jenkins\\projects\\${product}
        C:\\python311\\python.exe -c "from uatf.run import RunTests;RunTests().run_tests()" --CREATE_REPORT_UI True --HEADLESS_MODE True --WORKSPACE ${WORKSPACE}
        """

        bat(
        script: command_start_tests
        )

    archiveArtifacts "artifacts.zip"
    junit keepLongStdio: true, skipOldReports: true, testResults: 'test-reports/*.xml'
}}