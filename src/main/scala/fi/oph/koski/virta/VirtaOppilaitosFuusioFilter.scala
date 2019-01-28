package fi.oph.koski.virta

import fi.oph.koski.log.Logging

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, NodeSeq}

class VirtaOppilaitosFuusioFilter(sallitutMyöntäjät: List[String], sallitutFuusioMyöntäjät: List[String]) extends Logging {
  val fuusioitunutMyöntäjäKoodi = "5"

  def poistaDuplikaattisuoritukset(oppijaOidsForLogging: List[String])(virtaXml: Node): Node = {
    var poistojaTehty = Set.empty[String]
    val transformedXml = new RuleTransformer(new RewriteRule {
      override def transform(node: Node): Seq[Node] = if (karsittavaDuplikaatti(virtaXml, node)) {
        poistojaTehty = poistojaTehty + (node \ "@avain").text
        NodeSeq.Empty
      } else {
        node
      }
    }).transform(virtaXml).head

    if (poistojaTehty.nonEmpty) {
      logger.warn(s"Duplikaattisuorituksia poistettu ${poistojaTehty.size} kpl oppijalta $oppijaOidsForLogging")
    }

    transformedXml
  }

  private def karsittavaDuplikaatti(virtaXml: Node, node: Node) = {
    fuusioSuoritus(node) &&
    fuusioMyöntäjäSallittu(node) &&
    onDuplikaatti(virtaXml, node)
  }

  private def fuusioSuoritus(n: Node) = {
    n.label == "Opintosuoritus" &&
    (n \\ "Organisaatio").exists { o =>
      (o \ "Rooli").exists(_.text == fuusioitunutMyöntäjäKoodi) &&
      (o \ "Koodi").map(_.text).exists(sallitutMyöntäjät.contains)
    }
  }

  private def fuusioMyöntäjäSallittu(opintosuoritus: Node) =
    (opintosuoritus \ "Myontaja").map(_.text).forall(sallitutFuusioMyöntäjät.contains)

  def onDuplikaatti(virtaXml: Node, opintosuoritus: Node): Boolean = {
    val avain = (opintosuoritus \ "@avain").text
    (virtaXml \\ "Opintosuoritus").count(n => (n \ "@avain").text == avain) > 1
  }
}
