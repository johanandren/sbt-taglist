import bintray.Keys._

sbtPlugin := true

name := "taglist-plugin"

version := "1.4-SNAPSHOT"

organization := "com.markatta"

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

crossBuildingSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")

scalacOptions += "-unchecked"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.11" % "test"
)

resolvers ++= Seq("releases"  at "http://oss.sonatype.org/content/repositories/releases")

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization in bintray := None