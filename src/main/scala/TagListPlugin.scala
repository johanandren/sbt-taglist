package com.markatta.sbttaglist

import scala.io.{Codec, Source}
import sbt.{Def, _}
import Keys._

object TagListPlugin extends AutoPlugin {


  override def trigger = allRequirements
  override def requires = empty

  sealed trait LogLevel {
    def log(log: Logger, message: String)
  }
  case object Info extends LogLevel { def log(log: Logger, message: String) { log.info(message) } }
  case object Warn extends LogLevel { def log(log: Logger, message: String) { log.warn(message) } }
  case object Error extends LogLevel { def log(log: Logger, message: String) { log.error(message) } }

  type LineNumber = Int
  type Word = String
  type Line = String
  type TagList = Seq[(File, Seq[(LineNumber, Word, LogLevel)])]

  case class Tag(word: Word, level: LogLevel = Warn)

  object autoImport {
    lazy val tagListWords = SettingKey[Set[String]]("tag-list-words", "Tag words to look for when searching for tagged files")
    lazy val tags = SettingKey[Set[Tag]]("tag-list-tags", "Detailed tags with a log level per tag word")
    lazy val tagListSkipChars = SettingKey[Set[Char]]("tag-list-skip-chars", "Characters to skip around tag words")
    lazy val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    tagListWords := Set("todo", "fixme"),
    tags := tagListWords.value.map(word => Tag(word, Warn)),
    tagListSkipChars := Set('/', ':'),
    tagList := {
      val srcs = (sources in Compile).value
      val tagWords = tags.value
      val s = streams.value
      val skip = tagListSkipChars.value


      val tagList = TagListGenerator.generateTagList(srcs, tagWords, skip, Codec.UTF8)

      val count = tagList.map(_._2.size).sum
      if (count > 0) {
        s.log.warn("Tags found: %s" format count)
      }

      for {
        (file, tags) <- tagList
        (lineNumber, tagLine, logLevel) <- tags
      } {
        logLevel.log(s.log, file.getName + ":" + lineNumber + ": " + tagLine.trim)
      }

      tagList
    }
  )




}
