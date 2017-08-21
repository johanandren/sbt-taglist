package com.markatta.sbttaglist

import org.scalatest.{Matchers, WordSpec}

class TrieSpec extends WordSpec with Matchers {

  "A Trie" should {

    "be created" in {
      val result = Trie() :+ "wo" :+ "wi"

      result.children.size should === (1)
      val w = result.children('w')
      w.children.size should === (2)
      val o = w.children('o')
      o.children.size should === (0)
      o.isWord should === (true)
      val i = w.children('i')
      i.children.size should === (0)
      i.isWord should === (true)
    }

    "find defined strings" in {
      val trie = Trie() :+ "wo" :+ "wi" :+ "wizard"

      trie.contains("wo".iterator) should === (true)
      trie.contains("wi".iterator) should === (true)
      trie.contains("wizard".iterator) should === (true)
    }

    "not find undefined strings" in {
      val trie = Trie() :+ "wo" :+ "wi" :+ "wizard"
      trie.contains("murder".iterator) should === (false)
    }

  }

}
