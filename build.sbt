sbtPlugin := true

name := "taglist-plugin"

version := "1.1-SNAPSHOT"

organization := "com.markatta"

scalacOptions += "-unchecked"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.11" % "test"
)

resolvers ++= Seq("releases"  at "http://oss.sonatype.org/content/repositories/releases")
