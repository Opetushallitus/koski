package fi.oph.koski.migri

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue


object MigriSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[MigriOppija]).asInstanceOf[ClassSchema])
}

case class MigriOppija(
  henkilö: MigriHenkilo,
  opiskeluoikeudet: List[MigriOpiskeluoikeus]
)

case class MigriHenkilo(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kansalaisuus: Option[List[Koodistokoodiviite]]
)

case class MigriOpiskeluoikeus(
  oid: Option[String],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[MigriOppilaitos],
  tila: MigriOpiskeluoikeudenTila,
  arvioituPäättymispäivä: Option[LocalDate],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  tyyppi: Koodistokoodiviite,
  lisätiedot: Option[MigriOpiskeluoikeudenLisätiedot],
  suoritukset: List[MigriSuoritus],
  @KoodistoUri("virtaopiskeluoikeudenluokittelu")
  luokittelu: Option[List[Koodistokoodiviite]] = None
)

case class MigriOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[MigriOpiskeluoikeusJakso]
)

case class MigriOpiskeluoikeusJakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
)

case class MigriOppilaitos(
  nimi: Option[LocalizedString]
)

case class MigriOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite],
  lukukausiIlmoittautuminen: Option[MigriLukukausi_Ilmoittautuminen],
  maksettavatLukuvuosimaksut: Option[List[KorkeakoulunOpiskeluoikeudenLukuvuosimaksu]],
  majoitusetu: Option[Aikajakso],
  majoitus: Option[List[Aikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]],
  ulkomaanjaksot: Option[List[Aikajakso]],
  koulutusvienti: Option[Boolean]
)

case class MigriLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[MigriLukukausi_Ilmoittautumisjakso]
)

case class MigriLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: Koodistokoodiviite,
  maksetutLukuvuosimaksut: Option[MigriLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

case class MigriLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

case class MigriSuoritus(
  koulutusmoduuli: MigriSuorituksenKoulutusmoduuli,
  vahvistus: Option[MigriVahvistus],
  suoritustapa: Option[Koodistokoodiviite],
  suorituskieli: Option[Koodistokoodiviite],
  tyyppi: Koodistokoodiviite,
  tutkintonimike: Option[List[Koodistokoodiviite]],
  alkamispäivä: Option[LocalDate],
  osaamisenHankkimistapa: Option[List[MigriOsaamisenHankkimistapajakso]],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  oppimäärä: Option[Koodistokoodiviite],
  suoritettuErityisenäTutkintona: Option[Boolean],
  luokka: Option[String],
  pakollisetKokeetSuoritettu: Option[Boolean],
  osasuoritukset: Option[List[MigriOsasuoritus]],
  @KoodistoUri("virtaopsuorluokittelu")
  luokittelu: Option[List[Koodistokoodiviite]] = None
)

case class MigriSuorituksenKoulutusmoduuli(
  tunniste: MigriKoodiviite,
  diplomaType: Option[Koodistokoodiviite],
  nimi: LocalizedString,
  laajuus: Option[Laajuus]
)

case class MigriKoodiviite(
  koodiarvo: String,
  nimi: Option[LocalizedString],
  lyhytNimi: Option[LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
)

case class MigriVahvistus(
  päivä: LocalDate
)

case class MigriOsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: MigriOsaamisenHankkimistapa
)

case class MigriOsaamisenHankkimistapa(
  tunniste: Koodistokoodiviite,
  oppisopimus: Option[MigriOppisopimus]
)

case class MigriOppisopimus(
  työnantaja: Yritys
)

case class MigriOsasuoritus(
  koulutusmoduuli: MigriOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[MigriArviointi]],
  tyyppi: Koodistokoodiviite,
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite],
  tunnustettu: Option[MigriOsaamisenTunnustaminen], //vain jos lisätiedoissta löytyy koodiarvo "mukautettu"
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]],
  osasuoritukset: Option[List[MigriOsasuorituksenOsasuoritus]],
  suoritettuErityisenäTutkintona: Option[Boolean]
)

case class MigriOsasuorituksenOsasuoritus(
  koulutusmoduuli: MigriOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[MigriArviointi]],
  tyyppi: Koodistokoodiviite,
  tunnustettu: Option[MigriOsaamisenTunnustaminen], //vain jos lisätiedoissta löytyy koodiarvo "mukautettu"
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
)

case class MigriOsasuorituksenKoulutusmoduuli(
  tunniste: MigriKoodiviite,
  nimi: LocalizedString,
  oppimäärä: Option[Koodistokoodiviite],
  kieli: Option[Koodistokoodiviite],
  pakollinen: Option[Boolean],
  laajuus: Option[Laajuus]
)

case class MigriArviointi(
  arvosana: MigriKoodiviite,
  hyväksytty: Boolean,
  arviointiPäivä: Option[LocalDate]
)

case class MigriOsaamisenTunnustaminen(
  selite: LocalizedString
)
