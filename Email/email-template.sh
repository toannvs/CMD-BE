CURRENT_DATE=$3
BUILDVERSION=$2
BUILD_STATUS=$1

case $BUILD_STATUS in
	SUCCESS)
		COLOR="green"
	;;
	FAILURE)
		COLOR="red"
	;;
	ABORTED)
		COLOR="black"
	;;
	UNSTABLE)
		COLOR="yellow"
	;;
	*)
		COLOR="gray"
	;;
esac
cat <<EOL > templates.html
<!DOCTYPE html>
<html>
<body>
<style>
h3,h5 {
color:rgb(192,0,0)
}
table, th, td {
  border: 1px solid black;
  border-collapse: collapse;
}
th, td {
  padding: 5px;
}
</style>
<h3 style="text-align: center;color:blue;">THIS IS JENKINS MAIL NOTIFICATION</h3>

<p>Enviroment: <span style="color:rgba(29, 0, 192, 0.87)">CI/CD Ubuntu Server</span></p>

<p>Project Name: <span style="color:rgb(29, 0, 192, 0.87)">Co May Dorm Management</span></p>

<p><b>Build Status: </b><span style="color:$COLOR"><b>$BUILD_STATUS</b></span></p>


<p><a href="$JENKINS_URL/job/$PROJECT_NAME"><b>CLICK HERE!</b></a> to check console output.</p>

<p>
	<ul>
		<li>Date and Time: $BUILD_TIMESTAMP</li>
		<li>Environment: <b>Co May Dorm Management</b></li>
		<li>Any request to server: Yes</li>
		<li>Which server to access: <b>CMD environment servers</b></li> 
		<li>How long the deployment takes (if there are code changes): 5 mins (after receiving this email)</li>
		<li>Who will take charge on the deployment: <b>DUNG NGUYEN</b></li>
	</ul>
</p>

 <footer>
  <h3><span style="color:blue">DON'T REPLY THIS EMAIL!</span></h3>
  <p><span>Contact for support: </span><a href="mailto:nguyenminhdungtd98@gmail.com">Nguyen Minh Dung</a></p>
  <p>Thanks team!!!</p>
 </footer>

</body>
</html>
EOL
