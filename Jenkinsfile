pipeline {
	environment {
    		registry = "wayapaychat-container-registry/waya-temporal-wallet-service-staging"
    		registryCredential = 'DigitalOcean-registry-for-development'
    		dockerImage = ''
    	}
    
	agent any
	options {
		skipStagesAfterUnstable()
		disableConcurrentBuilds()
		parallelsAlwaysFailFast()
	}

   	tools {
        	jdk 'jdk-11'
        	maven 'mvn3.6.3'
    	}

	stages {
		
		 stage('Checkout') {
            		steps {
				sh "git branch"
                		sh "ls -lart ./*"
            		}
        	}     
		
        	stage('compile') {
            		steps {
               			sh "mvn clean install "
            		}
         }
    
		
		stage('Code Quality Check via SonarQube') {
			steps {
				script {
        				def scannerHome = tool 'Jenkins-sonar-scanner';
					def mvn         = tool 'mvn3.6.3'
					withSonarQubeEnv("Jenkins-sonar-scanner") {
          					sh "${mvn}/bin/mvn sonar:sonar"
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
       
   		stage('Remove Unused docker image') {
      			steps{
				cleanWs()
         			/* sh "docker rmi $registry:$BUILD_NUMBER" */
	   			sh "docker rmi $registry"
      			}
    		}
		
		stage ('Starting the deployment job') {
			steps {
                		build job: 'waya-staging-services/temporal-wallet-service', 
				parameters: [[$class: 'StringParameterValue', name: 'FROM_BUILD', value: "${BUILD_NUMBER}"]
	        			    ]
	    		}	    
    		}	 
    	}

}
