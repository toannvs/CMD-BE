#!/bin/bash

CURRENT_DATE=$3
BUILDVERSION=$2
BUILD_STATUS=$1

index=0
num=0
line=0
totalLine=0
CHANGE="No"
DBCHANGE="No"
declare -a tickets=()
declare -a authors=()
declare -a msgs=()


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
 <head>
  <title>Page Title</title>
  <style>
  table, th, td {
   border: 2px solid black;
   border-collapse: collapse;
  }
  td {text-align:center;}
  th {background-color:darkblue;color:white;}
  td.left {text-align:left;padding-left:10px}
  </style>
 </head>
 <body>
  <p>Dear all,</p>
  <p>This is Jenkins Mail notification<p>
  <p><b>Build Status: </b><span style="color:$COLOR"><b>$BUILD_STATUS</b></span></p>
  <p><u>$release</u></p>
  <p><b>Version:</b> $BUILDVERSION <p>
  <p>Please see below for ticket details:</p>
  <table style="width:100%">
  <tr>
   <th>No.</th>
   <th>System</th>
   <th style="width:150px">Author</th>
   <th style="width:160px">Ticket</th>
   <th>Description</th>
  </tr>
EOL


echo "</table>"  >> templates.html
cat <<-EOL >> templates.html
<p>
	<ul>
		<li>Date and Time: $CURRENT_DATE</li>
		<li>Environment: <b>Co May Dorm Management</b></li>
		<li>Any request to server: Yes</li>
		<li>Which server to access: <b>CMD environment servers</b></li> 
		<li>Any downtime requires: Yes</li>
		<li>How long the deployment takes (if there are code changes): 15 mins (after receiving this email)</li>
		<li>Who will take charge on the deployment: <b>DUC NGUYEN</b></li>
	</ul>
</p>
EOL
    
cat <<EOL >> templates.html
  </table>
 </body>
 <footer>
  <h3><span style="color:blue">DON'T REPLY THIS EMAIL!</span></h3>
  <p><span>Contact for support: </span><a href="mailto:nguyenminhdungtd98@gmail.com">nguyenminhdungtd98</a></p>
 </footer>
</html>
EOL