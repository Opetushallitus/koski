package fi.oph.koski.virta

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, NodeSeq}

object VirtaOppilaitosFuusioFilter {
  val fuusioitunutMyöntäjä = "5"
  def discardDuplicates(e: Node): Node = {
    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = if (isDuplicate(n)) {
        NodeSeq.Empty
      } else {
        n
      }
    }).transform(e).head
  }

  private def isDuplicate(n: Node) = if (n.label == "Opiskeluoikeus" || n.label == "Opintosuoritus") {
    (n \ "Organisaatio" \ "Rooli").headOption.exists(_.text == fuusioitunutMyöntäjä)
  } else {
    false
  }

}
