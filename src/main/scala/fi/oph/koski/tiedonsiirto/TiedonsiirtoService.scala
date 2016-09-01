package fi.oph.koski.tiedonsiirto

import fi.oph.koski.db.Tables.TiedonsiirtoRow
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.KoskiMessageField._
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import org.json4s.JsonAST.{JArray, JString}
import org.json4s._

class TiedonsiirtoService(tiedonsiirtoRepository: TiedonsiirtoRepository, organisaatioRepository: OrganisaatioRepository, oppijaRepository: OppijaRepository) {

  def findAll(koskiUser: KoskiUser): Seq[TiedonsiirtoRow] = {
    AuditLog.log(AuditLogMessage(TIEDONSIIRTO_KATSOMINEN, koskiUser, Map(juuriOrganisaatio -> koskiUser.juuriOrganisaatio.map(_.oid).getOrElse("ei juuriorganisaatiota"))))
    tiedonsiirtoRepository.findByOrganisaatio(koskiUser)
  }

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

  private def haeOidilla(oid: String)(implicit user: KoskiUser): Option[JValue] =
    oppijaRepository.findByOid(oid).map(o => toJValue(o.toHenkilötiedotJaOid))

  private def haeHetulla(hetu: String)(implicit user: KoskiUser): Option[JValue] =
    oppijaRepository.findOppijat(hetu).headOption.map(toJValue)
}

case class HetuTaiOid(oid: Option[String], hetu: Option[String])
