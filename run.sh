#Setup CLASSPATH to include all files in 'lib' folder.
#Change value of 'libs_path' variable to use a different location for libraries.

#export libs_path="$(pwd)/WEB-INF/lib/"
#export classes="$(pwd)/WEB-INF/classes/"
#export libs=$(find "$libs_path" -type f)
#export libs_conc=$(sed ':a;N;$!ba;s/\n/:/g'  <<< "$libs")
#export CLASSPATH="$classes:$libs_conc"
#export PATH="$PATH:$classes:$libs_conc"

#echo "PATH=$PATH" ##debug**
sudo systemctl restart tomcat9
#echo -e "\nRun complete."


