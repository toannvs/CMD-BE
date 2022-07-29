def emailSendingattachment (String recipients) {
  emailext (
    subject: "#Status: ${currentBuild.currentResult} #Job: ${env.JOB_NAME} #Build Number: ${env.BUILD_NUMBER}",                      
    attachmentsPattern: 'commit.txt',
    mimeType: 'text/html',
    body: """<p>See attached diff of <b>${env.JOB_NAME} #${env.BUILD_NUMBER}</b>.</p>
      <p>Check build changes on Jenkins <b><a href="${env.BUILD_URL}/last-changes">here</a></b>.</p>
      <p><b>Please do not reply this email</b>.</p>""",
    to: "${recipients}"
  )
}

def emailSendingnoattachment (String recipients) {
  env.currentDate = sh(returnStdout: true, script: 'date +%Y-%m-%d').trim()
                             def buildStatus = ""
                             withEnv(["buildStatus=${currentBuild.currentResult}"]) { 
                             sh '''
                               
                                     chmod +x ${WORKSPACE}/Email/email-template.sh
                                     echo ${currentDate}
                                     bash ${WORKSPACE}/Email/email-template.sh ${buildStatus} ${buildVersion} ${currentDate}
                                
                                
                             ''' 
                             }
                            emailext attachLog: true,
                                     subject: '$DEFAULT_SUBJECT',
                                     mimeType: 'text/html',
                                     body: '${FILE,path="templates.html"}',
                                     recipientProviders: [
                                     [$class: 'CulpritsRecipientProvider'],
                                     [$class: 'DevelopersRecipientProvider'],
                                     [$class: 'RequesterRecipientProvider']
                                     ], 
                                    //replyTo: '$DEFAULT_REPLYTO',
                                     to: "$recipients"
}

return this