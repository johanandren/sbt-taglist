import annotation.tailrec
import com.markatta.sbttaglist.Trie
import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {
  sealed trait LogLevel
  case object Warn extends LogLevel
  case object Info extends LogLevel
  case object Error extends LogLevel
  case object Debug extends LogLevel

  type TagList = Seq[(File, Seq[(String, Int, String)])]

  case class Tag(tag:String, level:LogLevel = Warn)
 
  object TagListKeys {
    val tagWords = SettingKey[Set[Tag]]("tag-list-words", "Tag words to look for when searching for tagged files")
    val skipChars = SettingKey[Set[Char]]("tag-list-skip-chars", "Characters to skip around tag words")
    val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set(Tag("todo"), Tag("fixme", Error)),
    skipChars := Set('/', ':')
  )
  
  def log(logger:Logger, msg:String, level:LogLevel) = level match {
    case Warn => logger.warn(msg)
    case Info => logger.info(msg)
    case Error => logger.error(msg)
    case Debug => logger.debug(msg)
  }

  lazy val tagListTask = tagList <<= (sources in Compile, tagWords, streams, skipChars) map {
    case (sources: Seq[File], tagWords: Set[Tag], streams: TaskStreams, skipChars: Set[Char]) => {
      val map = tagWords.map { t =>
        (Trie.skip(t.tag.toList, skipChars).mkString, t.level)
      }.toMap[String, LogLevel]

      val tagList = FileParser.generateTagList(sources, map.keys.toSet, skipChars)

      val count = tagList.foldLeft(0) { (acc, tags) =>
        acc + tags._2.length
      }

      val msg = "Tags found: %s" format count

      log(streams.log, msg, Info)
      log(streams.log, "-----------------------", Info)

      for (
        (file, tags) <- tagList;
        (tagName, lineNumber, tagLine) <- tags
      ) {
        map.get(tagName).map { level =>
          log(streams.log, "[%s] %s:%s: %s" format (tagName, file.getName, lineNumber, tagLine.trim), level)
        }
      }

      tagList
    }
  }

  private object FileParser {

    def generateTagList(files: Seq[File], tagWords: Set[String], skipChars:Set[Char]): TagList =
      files flatMap { file =>
        findTags(file, tagWords, skipChars) match {
          case Seq() => Seq()
          case foundTags => Seq(file -> foundTags)
        }
      }

    def findTags(file: File, tags: Set[String], skipChars:Set[Char]): Seq[(String, Int, String)] = {
      val trie = Trie(tags.map(_.toLowerCase))

      Source.fromFile(file).getLines.zipWithIndex.flatMap { case (line, number) =>
        trie.containsWordsInLine(line, skipChars).map((_, number, line))
      }.toSeq
    }
  }
}

