package fi.oph.koski.kela

import java.time.LocalDate

import fi.oph.koski.schema.{KoskiSchema, Aikajakso, Koodistokoodiviite, LocalizedString}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object KelaSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[KelaOppija]).asInstanceOf[ClassSchema])
}

case class KelaOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[Opiskeluoikeus]
)

case class Henkilo(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)

case class Opiskeluoikeus(
  oid: String,
  versionumero: String,
  aikaleima: LocalDate,
  oppilaitos: Oppilaitos,
  koulutustoimija: Koulutustoimija,
  sisältyyOpiskeluoikeuteen: Sisältäväopiskeluoikeus,
  arvioituPäättymispäivä: Option[LocalDate],
  ostettu: Option[Boolean],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[Suoritus],
  lisätiedot: OpiskeluoikeudenLisätiedot,
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  organisaatioHistoria: OrganisaatioHistoria
)

case class Sisältäväopiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

case class OpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
)

case class Opiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  opintojenRahoitus: Option[Koodistokoodiviite]
)

case class OrganisaatioHistoria(
  muutospäivä: LocalDate,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija]
)

case class OpiskeluoikeudenLisätiedot(
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean],
  majoitus: Option[List[Aikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]],
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Aikajakso]],
  erityinenTuki: Option[List[Aikajakso]],
  vaativanErityisenTuenErityinenTehtävä: Option[List[Aikajakso]],
  ulkomaanjaksot: Option[List[Aikajakso]],
  hojks: Option[Hojks],
  osaAikaisuusjaksot: Option[List[Aikajakso]],
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[Aikajakso]],
  vankilaopetuksessa: Option[List[Aikajakso]],
  ulkomainenVaihtoopiskelija: Option[Boolean],
  yksityisopiskelija: Option[Boolean],
  koulukoti: Option[List[Aikajakso]],
  majoitusetu: Option[Aikajakso],
  ulkomailla: Option[Aikajakso],
  tehostetunTuenPäätös: Option[Aikajakso],
  tehostetunTuenPäätökset: Option[List[Aikajakso]],
  joustavaPerusopetus: Option[Aikajakso]
)

case class Suoritus(
  koulutusmoduuli: SuorituksenKoulutusmoduuli,
  suoritustapa: Koodistokoodiviite,
  toimipiste: Toimipiste,
  oppimäärä: Option[Koodistokoodiviite],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: Koodistokoodiviite,
  tila: Option[Koodistokoodiviite],
  osaamisala: Option[List[Osaamisalajakso]],
  toinenOsaamisala: Option[Boolean],
  alkamispäivä: Option[LocalDate],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  tutkintonimike: Option[Koodistokoodiviite],
  toinenTutkintonimike: Option[Boolean],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  jääLuokalle: Option[Boolean],
  pakollisetKokeetSuoritettu: Option[Boolean],
  kokonaislaajuus: Option[Laajuus]
)

case class Osasuoritus(
  koulutusmoduuli: OsasuorituksenKoulutusmoduuli,
  liittyyTutkinnonOsaan: Option[Koodistokoodiviite],
  arviointi: Option[Arviointi],
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: Koodistokoodiviite,
  tila: Option[Koodistokoodiviite],
  tutkinto: Tutkinto,
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite],
  osaamisala: Option[List[Osaamisalajakso]],
  alkamispäivä: Option[LocalDate],
  tunnustettu: Option[OsaamisenTunnustaminen],
  toinenOsaamisala: Option[Boolean],
  toinenTutkintonimike: Option[Boolean],
  näyttö: Option[Näyttö],
  vastaavuusTodistuksenTiedot: Option[VastaavuusTodistuksenTiedot],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean],
  luokkaAste: Option[Koodistokoodiviite],
  tutkintokerta: Option[YlioppilastutkinnonTutkintokerta]
)

case class SuorituksenKoulutusmoduuli(
  tunniste: Koodistokoodiviite,
  laajuus: Option[Laajuus],
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[LocalizedString],
  koulutustyyppi: Option[Koodistokoodiviite],
  pakollinen: Option[Boolean],
  kuvaus: Option[LocalizedString],
  kieli: Option[Koodistokoodiviite],
  diplomaType: Option[Koodistokoodiviite],
  oppimäärä: Option[Koodistokoodiviite]
)

case class OsasuorituksenKoulutusmoduuli(
  tunniste: Koodistokoodiviite,
  laajuus: Option[Laajuus],
  perusteenNimi: Option[LocalizedString],
  pakollinen: Option[Boolean],
  kuvaus: Option[LocalizedString],
  kieli: Option[Koodistokoodiviite],
  osaAlue: Option[Koodistokoodiviite],
  taso: Option[Koodistokoodiviite],
  ryhmä: Option[Koodistokoodiviite],
  kurssinTyyppi: Option[Koodistokoodiviite],
  oppimäärä: Option[Koodistokoodiviite]
)


case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite
)

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
  laajuus: Laajuus
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tunniste: Koodistokoodiviite
)

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tunniste: Koodistokoodiviite
)

case class Osaamisalajakso(
  osaamisala: Koodistokoodiviite,
  alku: Option[LocalDate],
  loppu: Option[LocalDate]
)

case class OsaamisenTunnustaminen(
  osaaminen: Option[Osasuoritus],
  selite: LocalizedString,
  rahoituksenPiirissä: Option[Boolean]
)

case class Vahvistus(
  päivä: Option[LocalDate]
)

case class Arviointi(
  hyväksytty: Boolean,
  päivä: Option[LocalDate]
)

case class Laajuus(
  arvo: Double,
  yksikkö: Koodistokoodiviite
)

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[Koodistokoodiviite],
  nimi: Option[LocalizedString],
  kotipaikka: Option[Koodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[LocalizedString],
  yTunnus: String,
  kotipaikka: Option[Koodistokoodiviite]
)

case class Toimipiste(
  oid: String,
  nimi: Option[LocalizedString] = None,
  kotipaikka: Option[Koodistokoodiviite] = None
)

case class Hojks(
  opetusryhmä: Koodistokoodiviite,
  alku: Option[LocalDate],
  loppu: Option[LocalDate]
)

case class Näyttö(
  suorituspaikka: Option[NäytönSuorituspaikka],
  suoritusaika: Option[NäytönSuoritusaika],
  työssäoppimisenYhteydessä: Boolean,
  arviointi: Option[NäytönArviointi],
)

case class NäytönSuorituspaikka(
  tunniste: Koodistokoodiviite,
  kuvaus: LocalizedString
)

case class NäytönSuoritusaika(
  alku: LocalDate,
  loppu: LocalDate
)

case class NäytönArviointi(
  hyväksytty: Boolean
)

case class Tutkinto(
  tunniste: Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[LocalizedString],
  koulutustyyppi: Option[Koodistokoodiviite]
)

case class IBTheoryOfKnowledgeSuoritus(
  koulutusmoduuli: IBTheoryOfKnowledgeSuoritusKoulutusmoduuli,
  tila: Koodistokoodiviite,
  arviointi: Option[List[Arviointi]] = None,
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: Koodistokoodiviite
)

case class IBTheoryOfKnowledgeSuoritusKoulutusmoduuli(
  tunniste: Koodistokoodiviite,
  pakollinen: Option[Boolean]
)

case class IBExtendedEssaySuoritus(
  koulutusmoduuli: IBExtendedEssaySuoritusKoulutusmoduuli,
  tila: Koodistokoodiviite,
  arviointi: Option[List[Arviointi]] = None,
  tyyppi: Koodistokoodiviite
)

case class IBExtendedEssaySuoritusKoulutusmoduuli(
  tunniste: Koodistokoodiviite,
  pakollinen: Option[Boolean]
)

case class IBCASSuoritus(
  koulutusmoduuli: SuorituksenKoulutusmoduuli,
  arviointi: Option[List[Arviointi]] = None,
  tyyppi: Koodistokoodiviite,
  tila: Koodistokoodiviite
)

case class IBOppiaineExtendedEssay(
  tunniste: Koodistokoodiviite,
  aihe: LocalizedString,
  pakollinen: Option[Boolean]
)

case class YlioppilastutkinnonTutkintokerta(
  koodiarvo: String,
  vuosi: Int,
  vuodenaika: LocalizedString
)

case class VastaavuusTodistuksenTiedot(
  lukioOpintojenLaajuus: Laajuus
)
