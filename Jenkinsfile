pipeline {
    agent any
    environment {
        registry='863852973330.dkr.ecr.eu-west-1.amazonaws.com/waya-twallet-user'
        aws_Account_id='863852973330'
        aws_default_region='eu-west-1'
        image_repo_name='waya-infra-staging-registry'
        image_tag='latest'
    }
    tools {
        jdk 'jdk-11'
        maven 'mvn3.6.3'
    }
    stages{
        stage("compile") {
            steps{
                script {
                    sh 'mvn clean install'
                }
            }   
        }
        stage("Building Image") {
            steps{
                script {
                    dockerImage = docker.build registry
                }
            }   
        }
        stage("Logging into AWS ECR") {
            steps{
                script {
                    sh 'aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin 863852973330.dkr.ecr.eu-west-1.amazonaws.com'
                }
            }   
        }
       
        stage("pushing to ECR") {
            steps{
                script {
                    sh "aws ecr describe-repositories --repository-names waya-twallet-user || aws ecr create-repository --repository-name waya-twallet-user --image-scanning-configuration scanOnPush=true"
                    sh 'docker push 863852973330.dkr.ecr.eu-west-1.amazonaws.com/waya-twallet-user'
                }
            }   
        }
        stage("Deploying to EKS cluster") {
            steps{
                script {
                    withCredentials([kubeconfigFile(credentialsId: 'kuberenetes-config', variable: 'KUBECONFIG')]) {
                      dir('kubernetes/'){
                          
                          sh "helm upgrade --install waya-twallet ./base --kubeconfig ~/.kube/config \
                          --set ingress.enabled=false \
                          --set fullnameOverride=waya-twallet \
                          --set autoscaling.enaled=false \
                          --set service.type=ClusterIP \
                          --set service.port=9009 \
                          --set config.EUREKA_SERVER_URL=http://172.20.159.73:8761 \
                          --set config.url=jdbc:postgresql://waya-infra-staging-database-staging-env-staging.c7gddqax0vzn.eu-west-1.rds.amazonaws.com:5432/tempwalletDBstaging \
                          --set config.username=wayapayuser \
                          --set config.password=FrancisJude2020waya \
                          --set 'tolerations[0].effect=NoSchedule' \
                          --set 'tolerations[0].key=dev' \
                          --set 'tolerations[0].operator=Exists'"
                      }
                   }
                }
            }
        }   
    }
}
