package fi.oph.koski.virta

import fi.oph.koski.util.Timing

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, NodeSeq}

class VirtaOppilaitosFuusioFilter(sallitutMyöntäjät: List[String], sallitutFuusioMyöntäjät: List[String]) extends Timing {
  val fuusioitunutMyöntäjäKoodi = "5"

  def poistaDuplikaattisuoritukset(oppijaOidsForLogging: List[String])(virtaXml: Node): Node = timed("poistaDuplikaattisuoritukset") {
    val (transformedXml, removedNodesCount) = transform(oppijaOidsForLogging, virtaXml)

    if (removedNodesCount > 1) {
      logger.warn(s"Duplikaattisuorituksia poistettu $removedNodesCount kpl oppijalta $oppijaOidsForLogging")
    }

    transformedXml
  }

  private def transform(oppijaOidsForLogging: List[String], virtaXml: Node) = {
    // Caching is needed because RuleTransformers traverses each node multiple times and the "isDuplikaatti"-check
    // goes through the entire xml-document
    var duplikaattiCache = Map[Node, Boolean]()
    def isDuplikaattiFuusioSuoritus(node: Node): Boolean = {
      if (duplikaattiCache.contains(node)) {
        duplikaattiCache(node)
      } else {
        val isYhdistyneenKoulunDuplikaatti = isFromYhdistynytKoulu(node) && isMyöntäjäSallittu(node) && isDuplikaatti(virtaXml, node)
        duplikaattiCache = duplikaattiCache + (node -> isYhdistyneenKoulunDuplikaatti)
        isYhdistyneenKoulunDuplikaatti
      }
    }

    var poistojaTehty = Set.empty[String]
    (new RuleTransformer(new RewriteRule {
      override def transform(node: Node): Seq[Node] = if (isDuplikaattiFuusioSuoritus(node)) {
        poistojaTehty = poistojaTehty + (node \ "@avain").text
        NodeSeq.Empty
      } else {
        node
      }
    }).transform(virtaXml).head, poistojaTehty.size)
  }

  private def isFromYhdistynytKoulu(n: Node) = {
    (n.label == "Opiskeluoikeus" || n.label == "Opintosuoritus") &&
    (n \\ "Organisaatio").exists { o =>
      (o \ "Rooli").exists(_.text == fuusioitunutMyöntäjäKoodi) &&
      (o \ "Koodi").map(_.text).exists(sallitutMyöntäjät.contains)
    }
  }

  private def isMyöntäjäSallittu(n: Node) =
    (n \ "Myontaja").map(_.text).forall(sallitutFuusioMyöntäjät.contains)

  private def isDuplikaatti(virtaXml: Node, opintosuoritus: Node) = {
    val avain = (opintosuoritus \ "@avain").text
    (virtaXml \\ opintosuoritus.label).count(n => (n \ "@avain").text == avain) > 1
  }
}
