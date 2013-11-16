sbtPlugin := true

name := "taglist-plugin"

version := "1.3-SNAPSHOT"

organization := "com.markatta"

crossBuildingSettings

CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")

scalacOptions += "-unchecked"

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    None
  else
    Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/Code/johanandren.github.com/releases")))
}

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.11" % "test"
)

resolvers ++= Seq("releases"  at "http://oss.sonatype.org/content/repositories/releases")
