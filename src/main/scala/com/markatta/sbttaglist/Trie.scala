package com.markatta.sbttaglist

import annotation.tailrec

// we use a trie to search for multiple words in each line
case class Trie(children: Map[Char, Trie] = Map(), isWord: Boolean = false) {

  /** create a new trie with this trie and the given word */
  def :+(word: String): Trie = {
    // not tailrec, but words shouldn't be that long so probably ok.
    def add(prefix: List[Char], node: Trie): Trie = {
      prefix match {
        // mark up leafs
        case Nil => node.copy(isWord = true)

        // recursively add or update
        case head :: tail => {
          val nextTrie = node.children.getOrElse(head, Trie())
          node.copy(children = node.children.updated(head, add(tail, nextTrie)))
        }
      }
    }

    add(word.toList, this)
  }

  @tailrec
  private[this] def exists(word: List[Char], trie: Trie): Boolean = word match {
    // end of given word, is that node in the trie marked as a word?
    case Nil => trie.isWord

    case head :: tail => trie.children.get(head) match {
      // word does not exist in trie
      case None => false

      // word this far exists in trie
      case Some(nextTrie) => exists(tail, nextTrie)
    }
  }

  def contains(word:String, skipChars:Set[Char]):Boolean =
    exists(Trie.skip(word.toLowerCase.toList, skipChars), this)

  def containsWord(word: String, skipChars:Set[Char]): (Boolean, String) = {
    (contains(word, skipChars), word)
  }

  def containsAnyIn(line: String, skipChars:Set[Char] = Set()): Boolean =
    line.split(" ").exists(contains(_, skipChars))

  def containsWordsInLine(line: String, skipChars:Set[Char] = Set()): Seq[String] = {
    line.split(" ").map(containsWord(_, skipChars)).filter(_._1).map(_._2).map {
      word => Trie.skip(word.toList, skipChars).mkString
    }.distinct
  }
}

object Trie {
  def skip(word:List[Char], skipChars:Set[Char]) = {
    def drop(c:List[Char]) = skipChars.foldLeft(c) { (acc, w) =>
      acc.dropWhile(_ == w)
    } 

    drop(drop(word).reverse).reverse
  }

  def apply(words: Iterable[String]): Trie =
    words.foldLeft(Trie()){(trie, word) => trie :+ word }

}

