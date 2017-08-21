package com.markatta.sbttaglist

import org.scalatest.{Matchers, WordSpec}

class TextUtilSpec extends WordSpec with Matchers {

  "The TextUtils" should {

    "strip characters from both ends of a string" in {
      TextUtils.dropFromBothEnds(":word:", Set(':')) should === ("word".toSeq)
    }

  }
}