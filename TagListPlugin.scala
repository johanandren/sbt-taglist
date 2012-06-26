import io.Source
import sbt._
import Keys._

object TagListPlugin extends Plugin {

  type TagList = Seq[(File, Seq[(Int, String)])]
  
  object TagListKeys {
    val tagWords = SettingKey[Set[String]]("tag-words", "Tag words to look for when searching for tagged files")
  	val tagList = TaskKey[TagList]("tag-list", "Display all TODO tags in the sources of the project")
  }

  import TagListKeys._


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

  lazy val tagListSettings = Seq(
    tagListTask,
    tagWords := Set("TODO", "todo", "fixme")
  )


  private object FileParser {
    def generateTagList(files: Seq[File], tagWords: Set[String]): TagList =
      files flatMap { file =>
        findTags(file, tagWords) match {
          case Seq() => Seq()
          case foundTags => Seq(file -> foundTags)
        }
      }


    def findTags(file: File, tags: Set[String]): Seq[(Int, String)] =
      Source.fromFile(file).getLines.zipWithIndex.flatMap { case (line, number) =>
        if (tags.exists(line.contains(_))) {
          Some((number, line))
        } else {
          None
        }
      }.toSeq
  }

}
