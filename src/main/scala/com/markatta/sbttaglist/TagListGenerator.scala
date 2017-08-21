package com.markatta.sbttaglist

import com.markatta.sbttaglist.TagListPlugin._
import sbt.File

import scala.io.{Codec, Source}

object TagListGenerator {
  def generateTagList(files: Seq[File], tags: Set[Tag], skipChars: Set[Char], sourceEncoding: Codec): TagList = {
    val trie = Trie(tags.map(_.word.toLowerCase))
    val logLevelPerWord: Map[Word, LogLevel] = tags.map(t => t.word.toLowerCase -> t.level).toMap

    files.par.map { file =>
      var lineNumber = 0
      val lines = Source.fromFile(file)(sourceEncoding).getLines()
      val matchesInFile = Array.newBuilder[(LineNumber, Line, LogLevel)]

      while (lines.hasNext) {
        lineNumber += 1
        val line = lines.next()
        val wordIterator = line.toLowerCase.split(' ').iterator
        while(wordIterator.hasNext) {
          val cleanWord = TextUtils.dropFromBothEnds(wordIterator.next(), skipChars)
          if (trie.contains(cleanWord.iterator)) {
            matchesInFile += ((lineNumber, line, logLevelPerWord(cleanWord.mkString)))
          }
        }
      }

      file -> matchesInFile.result().toSeq
    }.seq
  }
}
