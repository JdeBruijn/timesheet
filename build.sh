#Setup CLASSPATH to include all files in 'lib' folder.
#Change value of 'libs_path' variable to use a different location for libraries.

export libs_path="$(pwd)/WEB-INF/lib/"
export classes="WEB-INF/classes/"
export sources="$(pwd)/WEB-INF/src/timesheet/"
export libs=$(find "$libs_path" -type f)
export libs_conc=$(sed ':a;N;$!ba;s/\n/:/g'  <<< "$libs")
export CLASSPATH="$classes:$libs_conc"

echo "CLASSPATH=$CLASSPATH" ##debug**

rm -rf  $classes*

javac -d "$classes" $sources*.java

echo -e "\nBuild complete."


