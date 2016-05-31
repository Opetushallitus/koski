package fi.oph.koski.api

import fi.oph.koski.util.XML.texts

import scala.collection.immutable.Seq
import scala.xml.Node

trait OpintosuoritusoteTestMethods extends SearchTestMethods {
  def opintosuoritusote(searchTerm: String, oppilaitosOid: String) = {
    searchForHenkilötiedot(searchTerm).map(_.oid).map { oppijaOid =>
      authGet(s"opintosuoritusote/${oppijaOid}/${oppilaitosOid}") {
        verifyResponseStatus(200)

        val lines: Seq[String] = scala.xml.XML.loadString(response.body).flatMap(_.descendant_or_self).flatMap {
          case tr: Node if tr.label == "tr" => Some(texts((tr \ "td") ++ (tr \ "th")))
          case h3: Node if h3.label == "h3" => Some(h3.text.trim)
          case _ => None
        }
        lines.mkString("\n").trim
      }
    }.headOption.getOrElse("")
  }
}
