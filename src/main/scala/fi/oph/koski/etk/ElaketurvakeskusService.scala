package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.Henkilö.Oid

import scala.io.BufferedSource


class ElaketurvakeskusService(application: KoskiApplication) {

  private def queryService = new ElaketurvakeskusQueryService(application.raportointiDatabase.db)

  def tutkintotiedot(request: Option[TutkintotietoRequest], source: Option[BufferedSource])(implicit koskiSession: KoskiSpecificSession): Option[EtkResponse] = {
    val ammatilliset = request.map(queryService.ammatillisetPerustutkinnot)
    val korkeakoulut = source.map(VirtaCsvParser.parse)
    val yhdistetty = (ammatilliset ++ korkeakoulut).reduceOption((a, b) => a.merge(b))
    val response = yhdistetty.map(TutkintotiedotFormatter.format)

    response.foreach(ElaketurvakeskusAuditLogger.auditLog(_, application))
    response
  }
}

case class EtkHenkilö(hetu: Option[String], syntymäaika: Option[LocalDate], sukunimi: String, etunimet: String)

case class EtkTutkinto(tutkinnonTaso: Option[String], alkamispäivä: Option[LocalDate], päättymispäivä: Option[LocalDate])

case class EtkViite(opiskeluoikeusOid: Option[String], opiskeluoikeusVersionumero: Option[Int], oppijaOid: Option[String])

case class EtkTutkintotieto(henkilö: EtkHenkilö, tutkinto: EtkTutkinto, viite: Option[EtkViite]) {
  def oid: Option[Oid] = viite.flatMap(_.oppijaOid)
}

case class EtkResponse(vuosi: Int, aikaleima: Timestamp, tutkintojenLkm: Int, tutkinnot: List[EtkTutkintotieto]) {
  def merge(other: EtkResponse): EtkResponse = {
    allowOnlySameYear(other)
    EtkResponse(
      vuosi,
      aikaleima,
      tutkintojenLkm + other.tutkintojenLkm,
      tutkinnot ::: other.tutkinnot
    )
  }

  private def allowOnlySameYear(other: EtkResponse) = {
    if (vuosi == other.vuosi) Unit else throw new Error(s"Vuosien ${vuosi} ja ${other.vuosi} tutkintotietoja yritettiin yhdistää")
  }
}
