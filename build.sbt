sbtPlugin := true

name := "taglist-plugin"

version := "1.2-SNAPSHOT"

organization := "com.markatta"

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
