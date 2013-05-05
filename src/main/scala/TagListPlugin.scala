import com.markatta.sbttaglist.{TextUtils, Trie}
import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {


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

  object TagListKeys {
    val tagWords = SettingKey[Set[String]]("tag-list-words", "Tag words to look for when searching for tagged files")
    val tags = SettingKey[Set[Tag]]("tag-list-tags", "Detailed tags with a log level per tag word")
    val skipChars = SettingKey[Set[Char]]("tag-list-skip-chars", "Characters to skip around tag words")
    val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set("todo", "fixme"),
    tags <<= tagWords(_.map(word => Tag(word, Warn))),
    skipChars := Set('/', ':')
  )

  lazy val tagListTask = tagList <<= (sources in Compile, tags, streams, skipChars) map {
    (sources: Seq[File], tagWords: Set[Tag], streams: TaskStreams, skipChars: Set[Char]) => {
      val tagList = generateTagList(sources, tagWords, skipChars)

      val count = tagList.map(_._2.size).sum
      if (count > 0) {
        streams.log.warn("Tags found: %s" format count)
      }

      for {
        (file, tags) <- tagList
        (lineNumber, tagLine, logLevel) <- tags
      } {
        logLevel.log(streams.log, file.getName + ":" + lineNumber + ": " + tagLine.trim)
      }

      tagList
    }

  }

 private def generateTagList(files: Seq[File], tags: Set[Tag], skipChars: Set[Char]): TagList = {

    val trie = Trie(tags.map(_.word.toLowerCase))
    val logLevelPerWord: Map[Word, LogLevel] = tags.map(t => t.word.toLowerCase -> t.level).toMap

    files.par.map { file =>

      val linesWithIndexes = Source.fromFile(file).getLines().zipWithIndex

      val matchesInFile = linesWithIndexes.foldLeft(Seq[(LineNumber, Line, LogLevel)]()) { case (acc, (line, lineNumber)) =>

        val foundWordsInLine = line.toLowerCase.split(' ').foldLeft(Seq[Word]()) { (wordAcc, word) =>
          val cleanWord = TextUtils.dropFromBothEnds(word.toList, skipChars).mkString
          if (trie.contains(cleanWord))
            wordAcc :+ cleanWord
          else
            wordAcc
        }


        acc ++ foundWordsInLine.map(word => (lineNumber, line, logLevelPerWord(word)))
      }

      file -> matchesInFile
    }.seq

  }


}