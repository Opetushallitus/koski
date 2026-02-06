package fi.oph.koski.todistus

import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.annotation.RedundantData
import fi.oph.koski.todistus.TodistusTemplateVariant.TodistusTemplateVariant
import fi.oph.koski.todistus.TodistusState.TodistusState

import java.time.LocalDateTime
import java.util.UUID

case class TodistusJob(
  id: String,
  @RedundantData // Piilotetaan loppukäyttäjiltä - voi olla eri kuin oppija itse (huoltaja, virkailija)
  userOid: Option[String],   // käyttäjä, joka tämän käynnisti
  oppijaOid: String,         // Oppija, jonka opiskeluoikeus on (nopeuttaa käyttöoikeustarkastuksia)
  opiskeluoikeusOid: String,
  templateVariant: TodistusTemplateVariant,
  opiskeluoikeusVersionumero: Option[Int], // Oo-versio, mistä pdf on luotu. Käytetään, kun tarkistetaan, pitääkö todistus luoda uudestaan.
  oppijaHenkilötiedotHash: Option[String], // Hash todistuksella näkyvistä oppijan henkilötiedoista (etunimet, sukunimi, syntymäaika jne.). Käytetään, kun tarkistetaan, pitääkö todistus luoda uudestaan.
  state: TodistusState = TodistusState.QUEUED,
  createdAt: LocalDateTime = LocalDateTime.now(),
  startedAt: Option[LocalDateTime] = None,
  completedAt: Option[LocalDateTime] = None,
  @RedundantData // Piilotetaan loppukäyttäjiltä
  worker: Option[String] = None,
  @RedundantData // Piilotetaan loppukäyttäjiltä
  attempts: Option[Int] = Some(0),
  error: Option[String] = None
) {
  def language: String = TodistusTemplateVariant.baseLanguage(templateVariant)
}

object TodistusState {
  type TodistusState = String

  val QUEUED = "QUEUED"
  val GATHERING_INPUT = "GATHERING_INPUT"
  val GENERATING_RAW_PDF = "GENERATING_RAW_PDF"
  val SAVING_RAW_PDF = "SAVING_RAW_PDF"
  val STAMPING_PDF = "STAMPING_PDF"
  val SAVING_STAMPED_PDF = "SAVING_STAMPED_PDF"
  val COMPLETED = "COMPLETED"
  val INTERRUPTED = "INTERRUPTED"
  val ERROR = "ERROR"
  // TODO: TOR-2400: Olisiko parempi vaan poistaa koko rivi ja tiedostot S3:sta suoraan, jos jotain halutaan ekspiroida? Riippuu siitä, miten siivousprosessi halutaan toteuttaa.
  val QUEUED_FOR_EXPIRE = "QUEUED_FOR_EXPIRE" // voi merkitä tämän, kun halutaan, että todistus poistetaan.
  val EXPIRED = "EXPIRED"// esim. siivousprosessi voi asettaa tämän, jos todistus on syystä tai toisesta vanhentunut (esim. oo mitätöity, tiedosto poistettu S3:sta tilan säästämiseksi tai muusta syystä jne.)

  val runningStates: Set[String] = Set(GATHERING_INPUT, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF)

  // States that should be re-queued if the worker lease is not active.
  val requeueableStates: Set[String] = runningStates + INTERRUPTED

  val nonReusableStates: Set[String] = Set(ERROR, QUEUED_FOR_EXPIRE, EXPIRED)

  val * : Set[String] = Set(QUEUED, GATHERING_INPUT, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF, COMPLETED, ERROR, QUEUED_FOR_EXPIRE, EXPIRED)
}

object TodistusTemplateVariant {
  type TodistusTemplateVariant = String

  val FI = "fi"
  val SV = "sv"
  val EN = "en"

  val * : Set[String] = Set(FI, SV, EN)

  def baseLanguage(variant: TodistusTemplateVariant): String = variant.take(2)
}

object TodistusJob {
  def apply(id: String, req: TodistusGenerateRequest, henkilötiedotHash: String, opiskeluoikeus: KoskiOpiskeluoikeusRow)(implicit user: KoskiSpecificSession): TodistusJob = TodistusJob(
    id = id,
    userOid = Some(user.oid),
    oppijaOid = opiskeluoikeus.oppijaOid,
    opiskeluoikeusOid = opiskeluoikeus.oid,
    templateVariant = req.templateVariant,
    opiskeluoikeusVersionumero = Some(opiskeluoikeus.versionumero),
    oppijaHenkilötiedotHash = Some(henkilötiedotHash)
  )
}
