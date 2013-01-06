import annotation.tailrec
import com.markatta.sbttaglist.Trie
import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {

  type TagList = Seq[(File, Seq[(Int, String)])]
  
  object TagListKeys {
    val tagWords = SettingKey[Set[String]]("tag-list-words", "Tag words to look for when searching for tagged files")
    val skipChars = SettingKey[Set[Char]]("tag-list-skip-chars", "Characters to skip around tag words")
    val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set("todo", "fixme"),
    skipChars := Set('/', ':')
  )

  lazy val tagListTask = tagList <<= (sources in Compile, tagWords, streams, skipChars) map {
    case (sources: Seq[File], tagWords: Set[String], streams: TaskStreams, skipChars:Set[Char]) => {
      val tagList = FileParser.generateTagList(sources, tagWords, skipChars)

      val count = tagList.foldLeft(0) { (acc, tags) =>
        acc + tags._2.length
      }

      streams.log.warn("Tags found: %s" format count)

      for (
        (file, tags) <- tagList;
        (lineNumber, tagLine) <- tags
      ) {
        streams.log.warn(file.getName + ":" + (lineNumber + 1) + ": " + tagLine.trim)
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

    def findTags(file: File, tags: Set[String], skipChars:Set[Char]): Seq[(Int, String)] = {
      val trie = Trie(tags.map(_.toLowerCase))

      Source.fromFile(file).getLines.zipWithIndex.flatMap { case (line, number) =>
        if (trie.containsAnyIn(line.toLowerCase, skipChars)) {
          Some((number, line))
        } else {
          None
        }
      }.toSeq
    }
  }

}
