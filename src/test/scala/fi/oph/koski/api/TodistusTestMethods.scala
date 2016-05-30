package fi.oph.koski.api

import fi.oph.koski.json.Json
import fi.oph.koski.schema._

import scala.collection.immutable.Seq
import scala.xml.{Node, XML}

trait TodistusTestMethods extends SearchTestMethods {
  def todistus(hetu: String) = {
    opiskeluoikeus(hetu).flatMap(_.id).map { opiskeluoikeusID =>
      authGet(s"todistus/opiskeluoikeus/${opiskeluoikeusID}") {
        verifyResponseStatus(200)
        val lines: Seq[String] = XML.loadString(response.body)
          .flatMap(_.descendant_or_self)
          .flatMap {
          case tr: Node if tr.label == "tr" && (tr \ "@class").text != "header" => Some((tr \ "td").map(_.text.trim).mkString(" ").trim)
          case h1: Node if List("h1", "h2").contains(h1.label) => Some(h1.text.trim)
          case h3: Node if h3.label == "h3" => Some((h3 \ "span").map(_.text.trim).mkString(" ").trim)
          case _ => None
        }
        lines.mkString("\n").trim
      }
    }.getOrElse("")
  }

  private def opiskeluoikeus(searchTerm: String): Option[Opiskeluoikeus] = {
    searchForHenkilötiedot(searchTerm).flatMap { henkilötiedot =>
      val oppija: Oppija = authGet("api/oppija/" + henkilötiedot.oid) {
        verifyResponseStatus(200)
        Json.read[Oppija](body)
      }
      oppija.opiskeluoikeudet.headOption
    }.headOption
  }
}
