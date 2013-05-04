package com.markatta.sbttaglist

import org.specs2.mutable.Specification

class TextUtilSpec extends Specification {

  "The TextUtils" should {

    "strip characters from both ends of a string" in {
      TextUtils.dropFromBothEnds(":word:", Set(':')) must beEqualTo ("word".toSeq)
    }

  }
}
