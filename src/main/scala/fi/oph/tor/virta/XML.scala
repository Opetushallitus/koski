package fi.oph.tor.virta

import scala.xml.{Node, PrettyPrinter}

object XML {
  def prettyPrint(xml: Node) = new PrettyPrinter(200, 2).format(xml)
}
