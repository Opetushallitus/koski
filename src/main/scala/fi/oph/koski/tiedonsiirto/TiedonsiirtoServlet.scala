package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.Tables.TiedonsiirtoRow
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import fi.oph.koski.servlet.ApiServlet
import fi.oph.koski.util.DateOrdering

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get() {
    kaikkiTiedonsiirrot
  }

  get("/virheet") {
    virheelliset
  }

  private def kaikkiTiedonsiirrot: List[HenkilönTiedonsiirrot] = toHenkilönTiedonsiirrot(application.tiedonsiirtoService.findAll(koskiUser))
  private def virheelliset: List[HenkilönTiedonsiirrot] =
    kaikkiTiedonsiirrot.filter { siirrot =>
      siirrot.rivit.groupBy(_.oppilaitos).exists { case (_, rivit) => rivit.headOption.exists(_.virhe.isDefined) }
    }.map(v => v.copy(rivit = v.rivit.filter(_.virhe.isDefined)))

  implicit val formats = Json.jsonFormats
  def toHenkilönTiedonsiirrot(tiedonsiirrot: Seq[TiedonsiirtoRow]) = {
    implicit val ordering = DateOrdering.localDateTimeReverseOrdering
    tiedonsiirrot.groupBy(_.oppija).map {
      case (oppijaOpt, rows) =>
        val oppija = oppijaOpt.flatMap(_.extractOpt[Henkilö])
        val rivit = rows.map { row =>
          val oppilaitos = row.oppilaitos.flatMap(_.extractOpt[List[OrganisaatioWithOid]])
          TiedonsiirtoRivi(row.aikaleima.toLocalDateTime, oppija, oppilaitos, row.virheet, row.data)
        }
        HenkilönTiedonsiirrot(oppija, rivit.sortBy(_.aika))
    }.toList.sortBy(_.rivit.head.aika)
  }

}

case class HenkilönTiedonsiirrot(oppija: Option[Henkilö], rivit: Seq[TiedonsiirtoRivi])
case class TiedonsiirtoRivi(aika: LocalDateTime, oppija: Option[Henkilö], oppilaitos: Option[List[OrganisaatioWithOid]], virhe: Option[AnyRef], inputData: Option[AnyRef])
case class Henkilö(oid: Option[String], hetu: Option[String], etunimet: Option[String], kutsumanimi: Option[String], sukunimi: Option[String], äidinkieli: Option[Koodistokoodiviite])
