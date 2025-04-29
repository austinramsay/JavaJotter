# JavaJotter Server
Server-side application for JavaJotter notes (https://github.com/austinramsay/JavaJotter)

Developed on a Sun Microsystems Ultra 45 Workstation, running Solaris 10 5/09, JDK version 1.6.0 update 45.

## Database
Uses an embedded Apache Derby database. The path to the database is a required argument to the JAR file to run.

Database username & password: javajotter

## Dependencies
Requires Apache Derby to be packaged with the JAR distributible.

Requires the JavaJotterLibrary.jar (https://github.com/austinramsay/JavaJotterLibrary) library to be packaged with the JAR distributible.

## Starting the Server
````
java -jar JavaJotterServer.jar jdbc:derby:/path/to/database
````
Example:

````
java -jar JavaJotterServer.jar jdbc:derby:/home/aramsay/JavaJotterServer/database/JavaJotter
````
