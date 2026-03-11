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
  isStamped: Boolean, // Onko todistus digitaalisesti allekirjoitettu Swisscomilla
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
  val EXPIRED = "EXPIRED"// Merkitään, kun todistus on vanhentunut. S3:sta siivous hoidetaan S3:n työkaluilla ja halutuilla säilytysajoilla.

  val runningStates: Set[String] = Set(GATHERING_INPUT, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF)

  // States that should be re-queued if the worker lease is not active.
  val requeueableStates: Set[String] = runningStates + INTERRUPTED

  val nonReusableStates: Set[String] = Set(ERROR, EXPIRED)

  val * : Set[String] = Set(QUEUED, GATHERING_INPUT, GENERATING_RAW_PDF, SAVING_RAW_PDF, STAMPING_PDF, SAVING_STAMPED_PDF, COMPLETED, ERROR, EXPIRED)
}

object TodistusTemplateVariant {
  type TodistusTemplateVariant = String

  val FI = "fi"
  val SV = "sv"
  val EN = "en"
  val fi_tulostettava_uusi = "fi_tulostettava_uusi"
  val fi_tulostettava_paivitys = "fi_tulostettava_paivitys"
  val sv_tulostettava_uusi = "sv_tulostettava_uusi"
  val sv_tulostettava_paivitys = "sv_tulostettava_paivitys"
  val en_tulostettava_uusi = "en_tulostettava_uusi"
  val en_tulostettava_paivitys = "en_tulostettava_paivitys"

  val * : Set[String] = Set(FI, SV, EN, fi_tulostettava_uusi, fi_tulostettava_paivitys, sv_tulostettava_uusi, sv_tulostettava_paivitys, en_tulostettava_uusi, en_tulostettava_paivitys)

  val printVariants: Set[String] = Set(fi_tulostettava_uusi, fi_tulostettava_paivitys, sv_tulostettava_uusi, sv_tulostettava_paivitys, en_tulostettava_uusi, en_tulostettava_paivitys)

  val kansalainenVariants: Set[String] = * -- printVariants

  def baseLanguage(variant: String): String = variant.take(2)

  def isKansalainenVariant(variant: String): Boolean = kansalainenVariants.contains(variant)
}

object TodistusJob {
  def apply(id: String, req: TodistusGenerateRequest, henkilötiedotHash: String, opiskeluoikeus: KoskiOpiskeluoikeusRow)(implicit user: KoskiSpecificSession): TodistusJob = TodistusJob(
    id = id,
    userOid = Some(user.oid),
    oppijaOid = opiskeluoikeus.oppijaOid,
    opiskeluoikeusOid = opiskeluoikeus.oid,
    templateVariant = req.templateVariant,
    opiskeluoikeusVersionumero = Some(opiskeluoikeus.versionumero),
    oppijaHenkilötiedotHash = Some(henkilötiedotHash),
    isStamped = !TodistusTemplateVariant.printVariants.contains(req.templateVariant)
  )
}
