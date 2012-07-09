import annotation.tailrec
import com.markatta.sbttaglist.Trie
import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {

  type TagList = Seq[(File, Seq[(Int, String)])]
  
  object TagListKeys {
    val tagWords = SettingKey[Set[String]]("tag-list-words", "Tag words to look for when searching for tagged files")
    val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set("todo", "fixme")
  )

  lazy val tagListTask = tagList <<= (sources in Compile, tagWords, streams) map {
    case (sources: Seq[File], tagWords: Set[String], streams: TaskStreams) => {

      val tagList = FileParser.generateTagList(sources, tagWords)

      for (
        (file, tags) <- tagList;
        (lineNumber, tagLine) <- tags
      ) {
        streams.log.warn(file.getName + ":" + lineNumber + ": " + tagLine.trim)
      }

      tagList
    }
  }


  private object FileParser {

    def generateTagList(files: Seq[File], tagWords: Set[String]): TagList =
      files flatMap { file =>
        findTags(file, tagWords) match {
          case Seq() => Seq()
          case foundTags => Seq(file -> foundTags)
        }
      }


    def findTags(file: File, tags: Set[String]): Seq[(Int, String)] = {
      val trie = Trie(tags)

      Source.fromFile(file).getLines.zipWithIndex.flatMap { case (line, number) =>
        if (trie.containsAnyIn(line.toLowerCase)) {
          Some((number, line))
        } else {
          None
        }
      }.toSeq
    }
  }

}
