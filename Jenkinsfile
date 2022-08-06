pipeline {
	agent any
	environment {
        report = '/var/lib/jenkins/workspace/CMD-BE/Email/email-template.html'
    }
    stages {
		stage ('Load functions') {      // Define the function files will be used
            steps {
                script {
                    emailFunction = load "Email/emailFunction.groovy"
                }
            }
        }
        stage('Build') { 
            steps {
				sh 'mvn clean install'
            }
        }        
		stage('Deploy') { 
            steps {
				sh 'sudo systemctl enable cmd.service'
				sh 'sudo systemctl stop cmd'
				sh 'sudo systemctl start cmd'
				sh 'sudo systemctl status cmd'
				sh 'rm -rf changelog*'
				sh "cp /var/lib/jenkins/jobs/CMD-BE/builds/${env.BUILD_NUMBER}/changelog* /var/lib/jenkins/workspace/CMD-BE"
            }
        }
	}
	post ('Send e-mail') {          // Stage for send an email
        always {
                script {
                    emailFunction.emailSendingnoattachment("nguyenminhdungtd98@gmail.com;comaydorm@gmail.com;toann7700@gmail.com")       // Define the emails address should be received the mail
                }
        }
    }

}

