ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

val scaLikeVersion = "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "sap-business-by-design"
  )

libraryDependencies ++= Seq( "org.scalikejdbc" %% "scalikejdbc" % scaLikeVersion,
  "org.scalikejdbc" %% "scalikejdbc-core" % scaLikeVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scaLikeVersion,
  "org.skinny-framework" %% "skinny-orm" % "3.0.0")
