package com.markatta.sbttaglist

import org.specs2.mutable._

class TrieSpec extends Specification {

  "A Trie" should {

    "be created" in {
      val result = Trie() :+ "wo" :+ "wi"

      result.children.size mustEqual (1)
      val w = result.children('w')
      w.children.size mustEqual (2)
      val o = w.children('o')
      o.children.size mustEqual (0)
      o.isWord must beTrue
      val i = w.children('i')
      i.children.size mustEqual (0)
      i.isWord must beTrue
    }

    "find defined strings" in {
      val trie = Trie() :+ "wo" :+ "wi" :+ "wizard"

      trie.contains("wo") must beTrue
      trie.contains("wi") must beTrue
      trie.contains("wizard") must beTrue
    }

    "not find undefined strings" in {
      val trie = Trie() :+ "wo" :+ "wi" :+ "wizard"
      trie.contains("murder") must beFalse
    }

  }

}
