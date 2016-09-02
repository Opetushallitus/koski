package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime

import fi.oph.koski.db.Tables.TiedonsiirtoRow
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import fi.oph.koski.util.DateOrdering
import org.json4s.JsonAST.{JArray, JString}
import org.json4s._

class TiedonsiirtoService(tiedonsiirtoRepository: TiedonsiirtoRepository, organisaatioRepository: OrganisaatioRepository, oppijaRepository: OppijaRepository) {
  def kaikkiTiedonsiirrot(koskiUser: KoskiUser): List[HenkilönTiedonsiirrot] = toHenkilönTiedonsiirrot(findAll(koskiUser))

  def virheelliset(koskiUser: KoskiUser): List[HenkilönTiedonsiirrot] =
    kaikkiTiedonsiirrot(koskiUser).filter { siirrot =>
      siirrot.rivit.groupBy(_.oppilaitos).exists { case (_, rivit) => rivit.headOption.exists(_.virhe.isDefined) }
    }.map(v => v.copy(rivit = v.rivit.filter(_.virhe.isDefined)))


  def storeTiedonsiirtoResult(implicit koskiUser: KoskiUser, data: Option[JValue], error: Option[TiedonsiirtoError]) {
    if (!koskiUser.isPalvelukäyttäjä) {
      return
    }

    val oppija = data.map(_ \ "henkilö").flatMap { henkilö =>
      val tunniste = Json.fromJValue[HetuTaiOid](henkilö)
      tunniste.oid.flatMap(haeOidilla)
        .orElse(tunniste.hetu.flatMap(haeHetulla))
        .orElse(henkilö.toOption)
    }

    val oppilaitokset = data.map(_ \ "opiskeluoikeudet" \ "oppilaitos" \ "oid").collect {
      case JArray(oids) => oids.collect { case JString(oid) => oid }
      case JString(oid) => List(oid)
    }.map(_.flatMap(organisaatioRepository.getOrganisaatio)).map(toJValue)

    koskiUser.juuriOrganisaatio.foreach(org => tiedonsiirtoRepository.create(koskiUser.oid, org.oid, oppija, oppilaitokset, error))
  }

  private def toHenkilönTiedonsiirrot(tiedonsiirrot: Seq[TiedonsiirtoRow]) = {
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

  private def findAll(koskiUser: KoskiUser): Seq[TiedonsiirtoRow] = {
    AuditLog.log(AuditLogMessage(TIEDONSIIRTO_KATSOMINEN, koskiUser, Map(juuriOrganisaatio -> koskiUser.juuriOrganisaatio.map(_.oid).getOrElse("ei juuriorganisaatiota"))))
    tiedonsiirtoRepository.findByOrganisaatio(koskiUser)
  }

  private def haeOidilla(oid: String)(implicit user: KoskiUser): Option[JValue] =
    oppijaRepository.findByOid(oid).map(o => toJValue(o.toHenkilötiedotJaOid))

  private def haeHetulla(hetu: String)(implicit user: KoskiUser): Option[JValue] =
    oppijaRepository.findOppijat(hetu).headOption.map(toJValue)
}

case class HenkilönTiedonsiirrot(oppija: Option[Henkilö], rivit: Seq[TiedonsiirtoRivi])
case class TiedonsiirtoRivi(aika: LocalDateTime, oppija: Option[Henkilö], oppilaitos: Option[List[OrganisaatioWithOid]], virhe: Option[AnyRef], inputData: Option[AnyRef])
case class Henkilö(oid: Option[String], hetu: Option[String], etunimet: Option[String], kutsumanimi: Option[String], sukunimi: Option[String], äidinkieli: Option[Koodistokoodiviite])
case class HetuTaiOid(oid: Option[String], hetu: Option[String])
