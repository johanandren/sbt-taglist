package com.markatta.sbttaglist

import scala.util.parsing.combinator.JavaTokenParsers

object TextUtils {

  def dropFromBothEnds(word: Seq[Char], charsToDrop: Set[Char]): Seq[Char] = {
    def skipLeading(word: Seq[Char]) =
      word.dropWhile(charsToDrop(_))

    if (word.nonEmpty) {
      val cleanLeading =
        if (charsToDrop(word.head))
          skipLeading(word)
        else
          word

      val cleanEnd =
        if (cleanLeading.nonEmpty && charsToDrop(cleanLeading.last))
          skipLeading(cleanLeading.reverse).reverse
        else
          cleanLeading

      cleanEnd
    } else {
      word
    }
  }

}
