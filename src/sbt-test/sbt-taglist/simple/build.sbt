lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.3",
    TaskKey[Unit]("check") := {
      val tags = tagList.value
      require(tags.size == 1, s"Expected [$tags] to contain 1 entry")
    }
  )