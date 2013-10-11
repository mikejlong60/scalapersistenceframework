The Scala Persistence Framework is a lightweight O/R mapper written in Scala. Following are 
directions for how to build it.  The easiest way to get the necessary setup is to 
download the Typesafe Scala stack(www.typesafe.com). The project's only dependencies are
a JDK >= 1.6 and the Scala 2.10 runtime environment and Velocity if you are going to use
the jdbcmetadata package. 

Dependencies:
1) Java 1.6 or greater
2) Scala 2.10

The project contains the following directory structure:

The Springframework is used only to provide a mock JNDI container. The commons* libraries provide connection 
pooling to simulate a runtime environment such as an application server or Tomcat.
The scalatest library is a Scala testing framework built on top of jUnit.  
And all you need is a JDBC driver. I use Postgres. But any other should work as well.

src/ - The Scala source code directory that includes main and test directories as per the default
SBT layout. 

To use the framework you need only have the dao.properties file at the root of your classpath. 
See the Scaladocs for how to define JNDI or other JDBC data sources. There are no other configuration
files.
