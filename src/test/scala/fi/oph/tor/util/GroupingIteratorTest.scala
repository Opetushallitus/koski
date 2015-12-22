package fi.oph.tor.util

import org.scalatest.{FreeSpec, Matchers}

class GroupingIteratorTest extends FreeSpec with Matchers{
  "GroupingIterator" - {
    "groups" in {
      GroupingIterator.groupBy(List(1,1,2,2,3,3,3,1).iterator)(num => num).toList.map(_._2.toList) should(equal(List(List(1, 1), List(2,2), List(3,3,3), List(1))))
    }
  }
}
