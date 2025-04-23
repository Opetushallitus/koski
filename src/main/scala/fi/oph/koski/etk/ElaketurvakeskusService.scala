package fi.oph.koski.etk

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OpintopolkuHenkilöRepository}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.Henkilö.Oid

import java.sql.Timestamp
import java.time.LocalDate
import scala.io.BufferedSource

class ElaketurvakeskusService(application: KoskiApplication) {

  private def queryService = new ElaketurvakeskusQueryService(application.raportointiDatabase.db)

  def tutkintotiedot(
    request: Option[TutkintotietoRequest],
    source: Option[BufferedSource],
    onr: OpintopolkuHenkilöRepository,
  )(implicit koskiSession: KoskiSpecificSession): Option[EtkResponse] = {
    val ammatilliset = request.map(queryService.ammatillisetPerustutkinnot)
    val korkeakoulut = source.map(VirtaCsvParser.parse).map(_.täydennäHenkilötiedot(onr))
    val yhdistetty = (ammatilliset ++ korkeakoulut).reduceOption((a, b) => a.merge(b))
    val response = yhdistetty.map(TutkintotiedotFormatter.format)

    response.foreach(ElaketurvakeskusAuditLogger.auditLog(_, application))
    response
  }
}

case class EtkHenkilö(hetu: Option[String], syntymäaika: Option[LocalDate], sukunimi: String, etunimet: String) {
  def täydennäHenkilötiedot(onr: OpintopolkuHenkilöRepository, oppijanumero: Option[String]): EtkHenkilö =
    oppijanumero match {
      case Some(oid) if hetu.isEmpty => onr.findByOid(oid).fold(this)(EtkHenkilö.apply)
      case _ => this
    }
}

object EtkHenkilö {
  def apply(henkilö: LaajatOppijaHenkilöTiedot): EtkHenkilö = EtkHenkilö(
    hetu = henkilö.hetu,
    syntymäaika = henkilö.syntymäaika,
    sukunimi = henkilö.sukunimi,
    etunimet = henkilö.etunimet,
  )
}

case class EtkTutkinto(tutkinnonTaso: Option[String], alkamispäivä: Option[LocalDate], päättymispäivä: Option[LocalDate])

case class EtkViite(opiskeluoikeusOid: Option[String], opiskeluoikeusVersionumero: Option[Int], oppijaOid: Option[String])

case class EtkTutkintotieto(henkilö: EtkHenkilö, tutkinto: EtkTutkinto, viite: Option[EtkViite]) {
  def oid: Option[Oid] = viite.flatMap(_.oppijaOid)

  def täydennäHenkilötiedot(onr: OpintopolkuHenkilöRepository): EtkTutkintotieto =
    copy(henkilö = henkilö.täydennäHenkilötiedot(onr, viite.flatMap(_.oppijaOid)))
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

  def täydennäHenkilötiedot(onr: OpintopolkuHenkilöRepository): EtkResponse = {
    val täydennetytTiedot = tutkinnot.map(_.täydennäHenkilötiedot(onr))
    copy(tutkintojenLkm = täydennetytTiedot.size, tutkinnot = täydennetytTiedot)
  }

  private def allowOnlySameYear(other: EtkResponse) = {
    if (vuosi == other.vuosi) Unit else throw new Error(s"Vuosien ${vuosi} ja ${other.vuosi} tutkintotietoja yritettiin yhdistää")
  }
}
