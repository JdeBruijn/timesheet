#!/bin/bash
#Setup CLASSPATH to include all files in 'lib' folder.
#Change value of 'libs_path' variable to use a different location for libraries.
#export libs_path="$(pwd)/WEB-INF/lib/"
#export libs=$(find "$(libs_path)" -type f)
#export libs_conc=$(sed ':a;N;$!ba;s/\n/:/g'  <<< "$libs")
#export CLASSPATH=$(libs_conc)
#echo "CLASSPATH=$CLASSPATH"

src=WEB-INF/src/timesheet/
classes = WEB-INF/classes/

Backend.class: $(src)Backend.java DatabaseHelper.class InvoiceData.class
	javac -d $(classes) $(src)Backend.java

DatabaseHelper.class: $(src)DatabaseHelper.java
	javac -d $(classes) $(src)DatabaseHelper.java

InvoiceData.class: $(src)InvoiceData.java
	javac -d $(classes) $(src)InvoiceData.java

run:
#	sudo service pulseaudio restart
#	bash /home/jannie/scripts/tomcat-stop.sh
#	bash /home/jannie/scripts/tomcat-start.sh
	sudo service tomcat restart

clean:
	rm -rf $(classes)*

logs1:
	tail --lines=200 $(CATALINA_HOME)/logs/catalina.out
	
logs2:
	tail --lines=200  $(CATALINA_HOME)/logs/localhost.*.log
