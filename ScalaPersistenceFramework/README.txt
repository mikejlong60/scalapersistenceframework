The Scala Persistence Framework is a lightweight O/R mapper written in Scala. Following are 
directions for how to build it.  The easiest way to get the necessary setup is to 
download the Typesafe Scala stack(www.typesafe.com). The project's only dependencies are
a JDK >= 1.6 and the Scala 2.10 runtime environment. See below for a few dependencies required
to run the Scalatest unit tests.

Dependencies:
1) Java 1.6 or greater
2) Scala 2.10

The project contains the following directory structure:

testlib/ - Contains the libraries needed to run the automated tests. 
These libraries are not needed at runtime in your application that uses the
Scala Persistence Framework.  The Springframework is used only 
to provide a mock JNDI container. The commons* libraries provide connection 
pooling to simulate a runtime environment such as an application server or Tomcat.
The scalatest library is a Scala testing framework built on top of jUnit.  
And all you need is a JDBC driver. I use Postgres. But any other should work as well.

scala/ - The Scala source code directory. 

scalatest/ - The directory containing scalatest artifacts. Note the dao.properties file.
It needs to be at the root of the classpath when you are using the framework and must
be called dao.properties. 


1) Run "ant clean scalatest-report" from the project root to run the tests from the command line
2) Run ant clean jar to produce a jar file containing only the framework classes, not the tests. 

To use the framework you need only have the dao.properties file at the root of your classpath. 
See the Scaladocs for how to define JNDI or other JDBC data sources. There are no other configuration
files.
