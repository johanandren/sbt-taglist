import annotation.tailrec
import com.markatta.sbttaglist.Trie
import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {
  import sbt.Level._

  type TagList = Seq[(File, Seq[(String, Int, String)])]

  case class Tag(tag:String, level:Level.Value = Warn)
 
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
  
  lazy val tagListTask = tagList <<= (sources in Compile, tagWords, streams, skipChars) map {
    case (sources: Seq[File], tagWords: Set[Tag], streams: TaskStreams, skipChars: Set[Char]) => {
      val map = tagWords.map { t =>
        (Trie.skip(t.tag.toLowerCase.toList, skipChars).mkString, t.level)
      }.toMap[String, Level.Value]

      val tagList = FileParser.generateTagList(sources, map.keys.toSet, skipChars)

      val (logs) = for (
        (file, tags) <- tagList;
        (tagName, lineNumber, tagLine) <- tags
      ) yield {
        map.get(tagName.toLowerCase).map { level =>
          (() => Logger.log(streams.log, 
            "[%s] %s:%s: %s" format (tagName, file.getName, lineNumber, tagLine.trim), level))
        }
      }

      Logger.log(streams.log, "Tags found: %s" format logs.length, Info)
      Logger.log(streams.log, "-----------------------", Info)

      logs.flatten.foreach(_())

      tagList
    }
  }

  private object Logger {

    def log(logger:Logger, msg:String, level:Level.Value) = level match {
      case Warn => logger.warn(msg)
      case Info => logger.info(msg)
      case Error => logger.error(msg)
      case Debug => logger.debug(msg)
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

    def findComments(lines:Iterator[String], l:String, r:String, single:Seq[String] = Seq()):Seq[String] = {
      def balanced(s:String):Boolean = {
        s.sliding(l.length).foldLeft(0) { (acc, a) => 
          val b = a match {
            case _ if a == l => 1
            case _ if a == r => -1
            case _ => 0
          } 
          b + acc
        } == 0
      }

      val (valid, _) = lines.foldLeft((Seq[String](), false)) ({ case ((acc, comment), line) =>
        val trimmed = line.trim
        if (comment) {
          if (trimmed.contains("*/")) {
            if (trimmed.endsWith("*/")) {
              (line +: acc, false)
            } else if (trimmed.startsWith("*/")) {
              (acc, false)
            } else {
              (line +: acc,  balanced(line))
            }
          } else {
            (line +: acc, true)
          }
        } else {
          if (single.exists(trimmed.startsWith(_))) {
            (line +: acc, false)
          } else if (trimmed.contains(l) || trimmed.contains(r)) {
            (line +: acc, balanced(line))
          } else {
            (acc, false)
          }
        }
      })

      valid.reverse
    }

    def findTags(file: File, tags: Set[String], skipChars:Set[Char]): Seq[(String, Int, String)] = {
      val trie = Trie(tags.map(_.toLowerCase))

      findComments(Source.fromFile(file).getLines, "/*", "*/", List("//")).zipWithIndex.flatMap { case (line, number) =>
        trie.containsWordsInLine(line, skipChars).map((_, number, line))
      }.toSeq

    }
  }
}

