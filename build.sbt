name := "ttv"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"
resolvers += DefaultMavenRepository

libraryDependencies ++= Seq(
//  "databricks" % "spark-corenlp" % "0.2.0-s_2.11",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  "org.apache.spark" %% "spark-core" % "1.6.1",
  "org.apache.spark" %% "spark-sql" % "1.6.1"
)