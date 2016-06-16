package fi.oph.koski.util

import scala.collection.immutable.Seq
import scala.xml.{Node, PrettyPrinter}

object XML {
  def prettyPrint(xml: Node) = new PrettyPrinter(200, 2).format(xml)
  def texts(nodes: Seq[Node]) = nodes.map(_.text).flatMap(_.split("\n")).map(_.trim).filterNot(_ == "").mkString(" ")
}
