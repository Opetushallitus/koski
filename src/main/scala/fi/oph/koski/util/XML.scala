package fi.oph.koski.util

import scala.xml.{Elem, Node, PrettyPrinter, Unparsed}


object XML {
  def prettyPrint(xml: Node) = new PrettyPrinter(200, 2).format(xml)
  def prettyPrintNodes(xml: Seq[Node]) = new PrettyPrinter(200, 2).formatNodes(xml)

  def texts(nodes: Seq[Node]) = nodes.map(_.text).flatMap(_.split("\n")).map(_.trim).filterNot(_ == "").mkString(" ")

  def transform(node: Node)(pf: PartialFunction[Node, Seq[Node]]): Seq[Node] =
    pf.applyOrElse(node, (node: Node) => node match {
      case e: Elem => e.copy(child = e.child.flatMap(n => transform(n)(pf)))
      case other => other
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
