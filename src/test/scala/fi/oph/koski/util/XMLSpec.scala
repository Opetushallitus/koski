package fi.oph.koski.util

import org.scalatest.{FreeSpec, Matchers}

import scala.xml.{Elem, NodeSeq, Text}

class XMLSpec extends FreeSpec with Matchers {
  "XML.transform" in {
    val node = <top><leaf1>a</leaf1><subtree><leaf2>b</leaf2><leaf3>c</leaf3></subtree></top>

    val transformed = XML.transform(node) {
      case e: Elem if e.label == "leaf1" => NodeSeq.Empty
      case e: Elem if e.label == "leaf2" => e.copy(child = e.child.flatMap(n => Text(n.text.toUpperCase)))
    }

    transformed should equal(<top><subtree><leaf2>B</leaf2><leaf3>c</leaf3></subtree></top>)
  }
}
