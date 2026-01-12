package fi.oph.koski.util

import scala.collection.immutable
import scala.xml.{Elem, Node, PrettyPrinter, Unparsed}

object XML {
  def copyElem(elem: Elem, children: immutable.Seq[Node]): Elem =
    new Elem(elem.prefix, elem.label, elem.attributes, elem.scope, elem.minimizeEmpty, children.toList: _*)

  def prettyPrint(xml: Node) = new PrettyPrinter(200, 2).format(xml)
  def prettyPrintNodes(xml: immutable.Seq[Node]) = new PrettyPrinter(200, 2).formatNodes(xml)

  def texts(nodes: immutable.Seq[Node]) = nodes.map(_.text).flatMap(_.split("\n")).map(_.trim).filterNot(_ == "").mkString(" ")

  def transform(node: Node)(pf: PartialFunction[Node, immutable.Seq[Node]]): immutable.Seq[Node] =
    pf.applyOrElse(node, (_: Node) =>
      node match {
        case e: Elem => copyElem(e, e.child.flatMap(n => transform(n)(pf)))
        case other   => other
      })

  /**
    * An XML node that outputs unescaped string data wrapped in CDATA,
    * with optional comment markers before and after.
    */
  object CommentedPCData {
    def apply(data: String, commentMarker: String = "// "): Node =
      Unparsed(s"$commentMarker<![CDATA[$data$commentMarker]]>")
  }
}
