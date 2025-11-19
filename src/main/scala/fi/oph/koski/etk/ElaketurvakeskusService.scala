package fi.oph.koski.etk

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.etk.EtkSukupuoli.EtkSukupuoli
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
    val korkeakoulut = source.flatMap(VirtaCsvParser.parse)
    val yhdistetty = (ammatilliset ++ korkeakoulut).reduceOption((a, b) => a.merge(b))
    val response = yhdistetty
      .map(_.täydennäHenkilötiedot(onr))
      .map(TutkintotiedotFormatter.format)

    response.foreach(ElaketurvakeskusAuditLogger.auditLog(_, application))
    response
  }
}

case class EtkHenkilö(hetu: Option[String], syntymäaika: Option[LocalDate], sukunimi: String, etunimet: String, sukupuoli: Option[EtkSukupuoli]) {
  def täydennäHenkilötiedot(onr: OpintopolkuHenkilöRepository, oppijanumero: String): EtkHenkilö =
    if (!tiedoissaPuutteita) this else {
      onr.findByOid(oppijanumero).fold(this)(fillMissing)
    }

  def tiedoissaPuutteita: Boolean = hetu.isEmpty || syntymäaika.isEmpty || sukupuoli.isEmpty

  def fillMissing(tiedot: LaajatOppijaHenkilöTiedot): EtkHenkilö = copy(
    hetu = List(hetu, tiedot.hetu).find(_.isDefined).flatten,
    syntymäaika = List(syntymäaika, tiedot.syntymäaika).find(_.isDefined).flatten,
    sukunimi = List(sukunimi, tiedot.sukunimi).find(_.nonEmpty).getOrElse(""),
    etunimet = List(etunimet, tiedot.etunimet).find(_.nonEmpty).getOrElse(""),
    sukupuoli = List(sukupuoli, EtkSukupuoli.fromOppijanumerorekisteri(tiedot)).find(_.isDefined).flatten,
  )
}

object EtkSukupuoli extends Enumeration {
  type EtkSukupuoli = Int
  val mies: Int = 1
  val nainen: Int = 2

  def fromOppijanumerorekisteri(t: LaajatOppijaHenkilöTiedot): Option[EtkSukupuoli] =
    t.sukupuoli match {
      case Some("1") => Some(EtkSukupuoli.mies)
      case Some("2") => Some(EtkSukupuoli.nainen)
      case _ => None
    }

  def fromVirta(sukupuoli: String): Option[EtkSukupuoli] =
    sukupuoli match {
      case "1" => Some(EtkSukupuoli.mies)
      case "2" => Some(EtkSukupuoli.nainen)
      case _ => None
    }

}

object EtkHenkilö {
  def apply(henkilö: LaajatOppijaHenkilöTiedot): EtkHenkilö = EtkHenkilö(
    hetu = henkilö.hetu,
    syntymäaika = henkilö.syntymäaika,
    sukunimi = henkilö.sukunimi,
    etunimet = henkilö.etunimet,
    sukupuoli = EtkSukupuoli.fromOppijanumerorekisteri(henkilö),
  )
}

case class EtkTutkinto(tutkinnonTaso: Option[String], alkamispäivä: Option[LocalDate], päättymispäivä: Option[LocalDate])

case class EtkViite(opiskeluoikeusOid: Option[String], opiskeluoikeusVersionumero: Option[Int], oppijaOid: Option[String])

case class EtkTutkintotieto(henkilö: EtkHenkilö, tutkinto: EtkTutkinto, viite: Option[EtkViite]) {
  def oid: Option[Oid] = viite.flatMap(_.oppijaOid)

  def täydennäHenkilötiedot(henkilötiedot: Map[String, LaajatOppijaHenkilöTiedot]): EtkTutkintotieto =
    copy(henkilö = viite
      .flatMap(_.oppijaOid)
      .flatMap(henkilötiedot.get)
      .map(henkilö.fillMissing)
      .getOrElse(henkilö)
    )
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
    val puutteelistenHenkilötietojenOidit = tutkinnot.flatMap { tutkinto =>
      if (tutkinto.henkilö.tiedoissaPuutteita) {
        tutkinto.viite.flatMap(_.oppijaOid).toList
      } else {
        List.empty
      }
    }
    val henkilötiedot = puutteelistenHenkilötietojenOidit
      .grouped(4000)
      .map(onr.henkilöt.findMasterOppijat)
      .fold(Map.empty)((a, b) => a ++ b)
    val täydennetytTiedot = tutkinnot.map(_.täydennäHenkilötiedot(henkilötiedot))
    copy(tutkintojenLkm = täydennetytTiedot.size, tutkinnot = täydennetytTiedot)
  }

  private def allowOnlySameYear(other: EtkResponse): Unit = {
    if (vuosi == other.vuosi) () else throw new Error(s"Vuosien ${vuosi} ja ${other.vuosi} tutkintotietoja yritettiin yhdistää")
  }
}
