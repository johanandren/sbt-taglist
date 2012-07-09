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

  def contains(word: String): Boolean = {
    @tailrec
    def exists(word: List[Char], trie: Trie): Boolean = word match {
      // end of given word, is that node in the trie marked as a word?
      case Nil => trie.isWord

      case head :: tail => trie.children.get(head) match {
        // word does not exist in trie
        case None => false

        // word this far exists in trie
        case Some(nextTrie) => exists(tail, nextTrie)
      }
    }
    exists(word.toList, this)
  }

  def containsAnyIn(line: String): Boolean =
    line.split(" ").exists(contains(_))

}

object Trie {

  def apply(words: Iterable[String]): Trie =
    words.foldLeft(Trie()){(trie, word) => trie :+ word }

}
