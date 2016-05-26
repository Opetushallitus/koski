package fi.oph.koski.api

import fi.oph.koski.json.Json
import fi.oph.koski.schema._
import org.scalatest.{FunSpec, Matchers}

import scala.collection.immutable.Seq
import scala.xml.{Node, XML}

class PerusopetusSpec extends FunSpec with Matchers with SearchTestMethods {
  describe("Perusopetuksen lisäopetus") {
    it("todistus") {
      todistus("200596-9755") should equal(
        """|Kymppiluokkalainen, Kaisa 200596-9755
          |
          |Äidinkieli ja kirjallisuus  Tyydyttävä  7
          |A1-kieli, englanti  Erinomainen  10
          |B1-kieli, ruotsi  Kohtalainen  6
          |Matematiikka  Kohtalainen  6
          |Biologia  Erinomainen  10
          |Maantieto  Kiitettävä  9
          |Fysiikka  Hyvä  8
          |Kemia  Kiitettävä  9
          |Terveystieto  Hyvä  8
          |Historia  Tyydyttävä 7
          |Yhteiskuntaoppi  Hyvä  8
          |Kuvataide  Hyvä 8
          |Liikunta  Tyydyttävä  7""".stripMargin)
    }
  }

  private def todistus(hetu: String) = {
    opiskeluoikeus(hetu).flatMap(_.id).map { opiskeluoikeusID =>
      authGet(s"todistus/opiskeluoikeus/${opiskeluoikeusID}") {
        verifyResponseStatus(200)
        val lines: Seq[String] = XML.loadString(response.body)
          .flatMap(_.descendant_or_self)
          .flatMap {
            case tr: Node if tr.label == "tr" && (tr \ "@class").text != "header" => Some((tr \ "td").map(_.text.trim).mkString(" ").trim)
            case h3: Node if h3.label == "h3" => Some((h3 \ "span").map(_.text.trim).mkString(" ").trim)
            case _ => None
        }
        lines.mkString("\n").trim
      }
    }.getOrElse("")
  }

  private def opiskeluoikeus(searchTerm: String): Option[PerusopetuksenLisäopetuksenOpiskeluoikeus] = {
    searchForHenkilötiedot(searchTerm).flatMap { henkilötiedot =>
      val oppija: Oppija = authGet("api/oppija/" + henkilötiedot.oid) {
        verifyResponseStatus(200)
        Json.read[Oppija](body)
      }
      oppija.opiskeluoikeudet.collect { case x: PerusopetuksenLisäopetuksenOpiskeluoikeus => x }.headOption
    }.headOption
  }
}
