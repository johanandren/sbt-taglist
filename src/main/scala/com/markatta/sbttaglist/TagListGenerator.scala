package com.markatta.sbttaglist

import com.markatta.sbttaglist.TagListPlugin._
import sbt.File

import scala.io.{Codec, Source}

object TagListGenerator {
  def generateTagList(files: Seq[File], tags: Set[Tag], skipChars: Set[Char], sourceEncoding: Codec): TagList = {

    val trie = Trie(tags.map(_.word.toLowerCase))
    val logLevelPerWord: Map[Word, LogLevel] = tags.map(t => t.word.toLowerCase -> t.level).toMap

    files.par.map { file =>

      val linesWithIndexes = Source.fromFile(file)(sourceEncoding).getLines().zipWithIndex

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
