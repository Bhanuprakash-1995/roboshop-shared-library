pipeline {
    agent {
        node {
            label 'AGENT-1'
        }
    }
    environment { 
        packageVersion = ''
        nexusUrl = '172.31.8.104:8081'
    }
     options {
        timeout(time: 1, unit: 'HOURS') 
        disableConcurrentBuilds()
        ansiColor('xterm')
    }
     parameters {
        booleanParam(name: 'Deploy', defaultValue: false, description: 'Confirm deploy?')
    }
    // build
    stages {
        stage('Get the version') {
            steps {
                script {
                    def packageJson = readJSON file: 'package.json'
                    packageVersion = packageJson.version
                    echo "application version: $packageVersion"
                }
            }
        }
        stage('Install dependencies') {
            steps {
                sh """
                    npm install
                """
            }
        }
        stage('Unit tests') {
            steps {
                sh """
                    echo "Unit test will run here.."
                """
            }
        }
        stage('Sonar Scan') {
            steps {
                sh """
                    sonar-scanner   
                """
            }
        }
         stage('Build') {
            steps {
                sh """
                   ls -la
                   zip -q -r catalogue.zip ./* -x ".git" -x "*.zip"
                   ls -ltr
                """
            }
        }
        stage('Publish Artifact') {
            steps {
              nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: "${nexusUrl}",
                    groupId: 'com.roboshop',
                    version: "${packageVersion}",
                    repository: 'catalogue',
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: 'catalogue',
                        classifier: '',
                        file: 'catalogue.zip',
                        type: 'zip']
                    ]
                )
            }
        }
        stage('Deploy') {
            when {
                expression {
                    params.Deploy == true
                }
            }
            steps {
                script {
                    def params =[
                        string(name: 'version', value: "$packageVersion")
                        // string(name: 'environment', value: "dev")
                    ]
                    build job: "catalogue-deploy", wait: true, parameters: params
                }
            }
        }
    }
    // post build
    post { 
        always { 
            echo 'I will always get executed irrespective of the pipeline status!'
            deleteDir()
        }
        failure { 
            echo 'This runs when pipeline is failed, used to send some alerts using slack..etc'
        }
        success { 
            echo 'This runs when pipeline executed successfully!'
        }
        aborted { 
            echo 'This runs when pipeline Timeout has been exceeded!'
        }
    }
}