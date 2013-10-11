name := "ScalaPersistenceFramework"

version :="0.2"

scalaVersion := "2.10.3"

//fork in Test := true

parallelExecution in Test := false

libraryDependencies += "velocity" % "velocity" % "1.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.springframework" % "spring-test" % "3.2.4.RELEASE" % "test"

libraryDependencies += "org.springframework" % "spring-jdbc" % "3.2.4.RELEASE" % "test"

libraryDependencies += "org.springframework" % "spring-core" % "3.2.4.RELEASE" % "test"

libraryDependencies += "commons-logging" % "commons-logging" % "1.1.3" % "test"

libraryDependencies += "commons-dbcp" % "commons-dbcp" % "1.4" % "test"

libraryDependencies += "commons-pool" % "commons-pool" % "1.6" % "test"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4" % "test"

