name := "DataMine"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation", "-diagrams", "-implicits", "-skip-packages", "samples")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.7" % "test",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.6",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.6",
  "ch.qos.logback" % "logback-classic" % "1.0.3",
  "com.datumbox" % "datumbox-framework-lib" % "0.7.0",
  "com.datumbox" % "datumbox-framework-core" % "0.7.0",
  "com.datumbox" % "datumbox-framework-common" % "0.7.0",
  "com.datumbox" % "datumbox-framework-applications" % "0.7.0"
)

scalacOptions in Test ++= Seq("-Yrangepos")

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 70
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true

//fork in run := true
//
//javaOptions in run ++= Seq(
//  "-Xms2G",
//  "-Xmx2G",
//  "-XX:MaxPermSize=2g",
//  "-XX:+UseConcMarkSweepGC"
//)

parallelExecution in Test := false