package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.{Instant, LocalDate}

import fi.oph.koski.config.KoskiApplication


class ElaketurvakeskusService(application: KoskiApplication) {

  private def queryService = new ElaketurvakeskusQueryService(application.raportointiDatabase.db)

  def ammatillisetPerustutkinnot(vuosi: Int, alku: LocalDate, loppu: LocalDate): EtkResponse = {
    val ammatillisetPerustutkinnotQueryResult: Seq[EtkTutkintotietoRow] = queryService.ammatillisetPerustutkinnotAikajaksolta(alku, loppu)
    val tutkintotiedot = ammatillisetPerustutkinnotQueryResult.map(toEtkTutkintotieto).toList

    EtkResponse(
      vuosi = vuosi,
      aikaleima = Timestamp.from(Instant.now),
      tutkintojenLkm = tutkintotiedot.size,
      tutkinnot = tutkintotiedot)
  }

  private def toEtkTutkintotieto(row: EtkTutkintotietoRow) = {
    EtkTutkintotieto(
      henkilö = EtkHenkilö(
        hetu = row.hetu,
        syntymäaika = row.syntymäaika.map(_.toLocalDate),
        sukunimi = row.sukunimi,
        etunimet = row.etunimet
      ),
      tutkinto = EtkTutkinto(
        tutkinnonTaso = Some(row.koulutusmuoto),
        alkamispäivä = row.alkamispaiva.map(_.toLocalDate),
        päättymispäivä = row.paattymispaiva.map(_.toLocalDate)
      ),
      viite = Some(EtkViite(
        opiskeluoikeusOid = Some(row.opiskeluoikeus_oid),
        opiskeluoikeusVersionumero = Some(row.versionumero),
        oppijaOid = Some(row.oppija_oid)
      ))
    )
  }
}

case class EtkHenkilö(hetu: Option[String], syntymäaika: Option[LocalDate], sukunimi: String, etunimet: String)

case class EtkTutkinto(tutkinnonTaso: Option[String], alkamispäivä: Option[LocalDate], päättymispäivä: Option[LocalDate])

case class EtkViite(opiskeluoikeusOid: Option[String], opiskeluoikeusVersionumero: Option[Int], oppijaOid: Option[String])

case class EtkTutkintotieto(henkilö: EtkHenkilö, tutkinto: EtkTutkinto, viite: Option[EtkViite])

case class EtkResponse(vuosi: Int, aikaleima: Timestamp, tutkintojenLkm: Int, tutkinnot: List[EtkTutkintotieto])
