package fi.oph.koski.util

import scala.collection.immutable.Seq
import scala.xml.{Atom, Node, PrettyPrinter}

object XML {
  def prettyPrint(xml: Node) = new PrettyPrinter(200, 2).format(xml)
  def texts(nodes: Seq[Node]) = nodes.map(_.text).flatMap(_.split("\n")).map(_.trim).filterNot(_ == "").mkString(" ")

  /** An XML node to output unescaped string data, wrapped around CDATA marker.
    *
    * See [[scala.xml.PCData]] for more.
    *
    * @param data the string to output without XML escaping
    * @param commentMarker marker string to prepend before opening and closing CDATA marker
    */
  class CommentedPCData(data: String, commentMarker: String = "// ") extends Atom[String](data) {
    override def buildString(sb: StringBuilder): StringBuilder =
      sb append s"$commentMarker<![CDATA[%s$commentMarker]]>".format(data)
  }

  object CommentedPCData {
    def apply(data: String): CommentedPCData = new CommentedPCData(data)
  }
}
