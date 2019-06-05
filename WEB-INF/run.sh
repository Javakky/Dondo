sudo cp /home/ec2-user/*.java src/
sudo sh compile.sh
sudo service tomcat7 stop
sudo service tomcat7 start
