pipeline {

    agent { label 'worker1' }

    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
        AWS_DEFAULT_REGION = credentials('AWS_DEFAULT_REGION')
        CLUSTER_NAME = credentials('CLUSTER_NAME')
        REGISTRY = credentials('REGISTRY')
        SERVICE_NAME = 'waya-repository'
        VERSION = sh (script: 'git rev-parse HEAD', returnStdout: true).trim().take(10)
        NAMESPACE = "${env.GIT_BRANCH == 'production' ? 'production' : 'staging' }"
    }


    stages{
        /*stage('Security Scan') {
            steps {
                withSonarQubeEnv('Waya Sonar') {
                    sh 'mvn clean verify sonar:sonar -DskipTests -Dsonar.projectKey=WAYA-PAY-CHAT-2.0-WAYA-REPOSITORY'
                }
            }
        }*/

        stage("build") {
            steps{
                script {
                    sh 'mvn clean install package -DskipTests'
                    echo 'Build with Maven'
                }
            }
        }

        stage('Image Build') {
            steps {
                script {
                    dockerImage = docker.build "${REGISTRY}/${SERVICE_NAME}:${VERSION}"
                }
            }
        }

        stage('ECR') {
            steps {
                script {
                    sh "aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin ${REGISTRY}"
                }
            }
        }

        stage('pushing to ECR') {
            steps {
                script {
                    sh "docker push ${REGISTRY}/${SERVICE_NAME}:${VERSION}"
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'staging'
            }
            steps {
                script {
                    sh '''
                        chmod +x ./deploy.sh
                        ./deploy.sh
                        '''
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'production'
            }
            steps {
                script {
                    sh '''
                        chmod +x ./deploy.sh
                        ./deploy.sh
                        '''
                }
            }
        }   
    }
}
