import com.markatta.sbttaglist.Trie
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
    val tagWords = SettingKey[Set[Tag]]("tag-list-words", "Tag words to look for when searching for tagged files")
    val skipChars = SettingKey[Set[Char]]("tag-list-skip-chars", "Characters to skip around tag words")
    val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set(Tag("todo"), Tag("fixme")),
    skipChars := Set('/', ':')
  )

  lazy val tagListTask = tagList <<= (sources in Compile, tagWords, streams, skipChars) map {
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

    files.map { file =>

      val lines = Source.fromFile(file).getLines()
      val matchesInFile = lines.zipWithIndex.foldLeft(Seq[(LineNumber, Line, LogLevel)]()) { case (acc, (line, lineNumber)) =>

        val foundWordsInLine = line.toLowerCase.split(' ').foldLeft(Seq[Word]()) { (wordAcc, word) =>
          val cleanWord = dropFromBothEnds(word.toList, skipChars).mkString
          if (trie.contains(cleanWord))
            wordAcc :+ cleanWord
          else
            wordAcc
        }


        acc ++ foundWordsInLine.map(word => (lineNumber, line, logLevelPerWord(word)))
      }

      file -> matchesInFile
    }

  }

  private def dropFromBothEnds(word: List[Char], skipChars: Set[Char]): List[Char] = {
    def skipLeading(word: List[Char]) =
      word.dropWhile(skipChars(_))

    if (word.nonEmpty) {
      val cleanLeading =
        if (skipChars(word.head))
          skipLeading(word)
        else
          word

      val cleanEnd =
        if (cleanLeading.nonEmpty && skipChars(cleanLeading.last))
          skipLeading(cleanLeading.reverse).reverse
        else
          cleanLeading

      cleanEnd
    } else {
      word
    }
  }
}