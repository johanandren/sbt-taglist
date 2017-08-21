lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.3",
    sources in tagList := (sources in Compile).value ++ (sources in Test).value,
    TaskKey[Unit]("check") := {
      val tags = tagList.value
      require(tags.size == 2, s"Expected [$tags] to contain 2 file entries")
    }
  )