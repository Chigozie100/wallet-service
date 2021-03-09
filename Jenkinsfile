pipeline {
	environment {
    		registry = "wayapaychat-container-registry/waya-temporal-wallet-service"
    		registryCredential = 'DigitalOcean-registry-for-development'
    		dockerImage = ''
    	}
      	/*	parameters {
	    strings(name: 'FROM_BUILD' defaultValue: '', description: 'Build Source')
	} */
    
	agent any

   	tools {
        	jdk 'jdk-11'
        	maven 'mvn3.6.3'
    	}

	stages {
		
		 stage('Checkout') {
            		steps {
				cleanWs()
				checkout([$class: 'GitSCM', branches: [[name: '*/dev']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'odenigbo-github-credentials', url: 'https://github.com/WAYA-MULTI-LINK/WAYA-PAY-CHAT-2.0-AUTH-SERVICE.git']]])
				sh "git branch"
                		sh "ls -lart ./*"
            		}
        	}     
		
        	stage('compile') {
            		steps {
               			sh "mvn clean install"
            		}
                }
		
		stage('Code Quality Check via SonarQube') {
			steps {
                 		script {
			     		def scannerHome = tool 'Jenkins-sonar-scanner';
                     			withSonarQubeEnv("Jenkins-sonar-scanner") {
                     				sh "${tool("Jenkins-sonar-scanner")}/bin/sonar-scanner \
		     				            -Dsonar.projectName=waya-temporal-wallet-service \
	             				      -Dsonar.projectKey=waya-temporal-wallet-service \
	             				      -Dsonar.sources=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev \
		     				            -Dsonar.projectBaseDir=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev \
		     				            -Dsonar.projectVersion=1.0 \
                     				-Dsonar.language=java \
                     				-Dsonar.java.binaries=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/target/classes \
                     				-Dsonar.sourceEncoding=UTF-8 \
                     				-Dsonar.exclusions=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/src/test/**/* \
		     				            -Dsonar.junit.reportsPath=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/target/surefire-reports \
                     				-Dsonar.surefire.reportsPath=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/target/surefire-reports \
                     				-Dsonar.jacoco.reportPath=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/target/coverage-reports/jacoco-unit.exec \
                     				-Dsonar.java.coveragePlugin=/var/jenkins_home/workspace/waya-2.0-waya-temporal-wallet-service-dev/target/jacoco  \
		     				            -Dsonar.host.url=https://sonarqube.waya-pay.com \
		     				            -Dsonar.verbose=true "
               				}
           			}
      		 	}
   		}
	    
		//stage("Quality Gate") {
			//steps {
				//timeout(time: 1, unit: 'HOURS') {
                    	    		// Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    	    		// true = set pipeline to UNSTABLE, false = don't
                   	    		//waitForQualityGate abortPipeline: true
                		//}
           	 	//}
       	 	//}
		
		stage('Building image') {
      			steps{
        			script {
          				/*dockerImage = docker.build registry + ":$BUILD_NUMBER" */
	    				dockerImage=docker.build registry
        			}
      			}
		}
    
		stage('Deploy Image') {
      			steps{
         			script {
		    			docker.withRegistry( 'https://registry.digitalocean.com/wayapaychat-container-registry', registryCredential ) {
            					dockerImage.push()
          				}
        			}
      			}
    		}
    
		stage ('Starting the deployment job') {
			steps {
                		build job: 'waya-2.0-waya-temporal-wallet-service-deploy-dev', 
				parameters: [[$class: 'StringParameterValue', name: 'FROM_BUILD', value: "${BUILD_NUMBER}"]
	        			    ]
	    		}	    
    		}	  
       
   		stage('Remove Unused docker image') {
      			steps{
         			/* sh "docker rmi $registry:$BUILD_NUMBER" */
	   			sh "docker rmi $registry"
      			}
    		} 
    	}

}
