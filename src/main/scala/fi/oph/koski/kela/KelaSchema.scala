package fi.oph.koski.kela

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

object KelaSchema {
  lazy val schemaJson: JValue = SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[KelaOppija]).asInstanceOf[ClassSchema])
}

case class KelaOppija(
  henkilö: Henkilo,
  opiskeluoikeudet: List[KelaOpiskeluoikeus]
)

case class Henkilo(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)

case class KelaOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  ostettu: Option[Boolean],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[Suoritus],
  lisätiedot: Option[OpiskeluoikeudenLisätiedot],
  tyyppi: schema.Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
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
  tila: schema.Koodistokoodiviite,
  opintojenRahoitus: Option[schema.Koodistokoodiviite]
)

case class OrganisaatioHistoria(
  muutospäivä: LocalDate,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija]
)

case class OpiskeluoikeudenLisätiedot(
  oikeusMaksuttomaanAsuntolapaikkaanPerusopetus: Option[schema.Aikajakso],
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean],
  majoitus: Option[List[schema.Aikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[schema.Aikajakso]],
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[schema.Aikajakso]],
  erityinenTuki: Option[List[schema.Aikajakso]],
  vaativanErityisenTuenErityinenTehtävä: Option[List[schema.Aikajakso]],
  ulkomaanjaksot: Option[Any],
  hojks: Option[Hojks],
  osaAikaisuusjaksot: Option[List[schema.OsaAikaisuusJakso]],
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[schema.OpiskeluvalmiuksiaTukevienOpintojenJakso]],
  vankilaopetuksessa: Option[List[schema.Aikajakso]],
  ulkomainenVaihtoopiskelija: Option[Boolean],
  yksityisopiskelija: Option[Boolean],
  koulukoti: Option[List[schema.Aikajakso]],
  majoitusetu: Option[schema.Aikajakso],
  ulkomailla: Option[schema.Aikajakso],
  tehostetunTuenPäätös: Option[schema.TehostetunTuenPäätös],
  tehostetunTuenPäätökset: Option[List[schema.TehostetunTuenPäätös]],
  joustavaPerusopetus: Option[schema.Aikajakso]
)

case class Suoritus(
  koulutusmoduuli: SuorituksenKoulutusmoduuli,
  suoritustapa: Option[schema.Koodistokoodiviite],
  toimipiste: Option[Toimipiste],
  oppimäärä: Option[schema.Koodistokoodiviite],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Option[Boolean],
  alkamispäivä: Option[LocalDate],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  tutkintonimike: Option[List[schema.Koodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
  theoryOfKnowledge: Option[IBTheoryOfKnowledgeSuoritus],
  extendedEssay: Option[IBExtendedEssaySuoritus],
  creativityActionService: Option[IBCASSuoritus],
  jääLuokalle: Option[Boolean],
  pakollisetKokeetSuoritettu: Option[Boolean],
  kokonaislaajuus: Option[schema.Laajuus]
)

case class Osasuoritus(
  koulutusmoduuli: OsasuorituksenKoulutusmoduuli,
  liittyyTutkinnonOsaan: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[Arviointi]],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  tutkinto: Option[Tutkinto],
  tutkinnonOsanRyhmä: Option[schema.Koodistokoodiviite],
  osaamisala: Option[List[schema.Osaamisalajakso]],
  alkamispäivä: Option[LocalDate],
  tunnustettu: Option[schema.OsaamisenTunnustaminen],
  toinenOsaamisala: Option[Boolean],
  toinenTutkintonimike: Option[Boolean],
  näyttö: Option[Näyttö],
  vastaavuusTodistuksenTiedot: Option[VastaavuusTodistuksenTiedot],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean],
  luokkaAste: Option[schema.Koodistokoodiviite],
  tutkintokerta: Option[YlioppilastutkinnonTutkintokerta]
)

case class SuorituksenKoulutusmoduuli(
  tunniste: schema.KoodiViite,
  laajuus: Option[schema.Laajuus],
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  pakollinen: Option[Boolean],
  kuvaus: Option[schema.LocalizedString],
  kieli: Option[schema.Koodistokoodiviite],
  diplomaType: Option[schema.Koodistokoodiviite],
  oppimäärä: Option[schema.Koodistokoodiviite]
)

case class OsasuorituksenKoulutusmoduuli(
  tunniste: schema.KoodiViite,
  laajuus: Option[schema.Laajuus],
  perusteenNimi: Option[schema.LocalizedString],
  pakollinen: Option[Boolean],
  kuvaus: Option[schema.LocalizedString],
  kieli: Option[schema.Koodistokoodiviite],
  osaAlue: Option[schema.Koodistokoodiviite],
  taso: Option[schema.Koodistokoodiviite],
  ryhmä: Option[schema.Koodistokoodiviite],
  kurssinTyyppi: Option[schema.Koodistokoodiviite],
  oppimäärä: Option[schema.Koodistokoodiviite]
)


case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: schema.Koodistokoodiviite,
  maa: schema.Koodistokoodiviite
)

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: schema.Koodistokoodiviite,
  maa: schema.Koodistokoodiviite,
  laajuus: schema.Laajuus
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tunniste: schema.Koodistokoodiviite
)

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tunniste: schema.Koodistokoodiviite
)

case class Vahvistus(
  päivä: LocalDate
)

case class Arviointi(
  hyväksytty: Boolean,
  päivä: Option[LocalDate]
)

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[schema.Koodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[schema.Koodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[schema.Koodistokoodiviite]
)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[schema.Koodistokoodiviite] = None
)

case class Hojks(
  opetusryhmä: schema.Koodistokoodiviite,
  alku: Option[LocalDate],
  loppu: Option[LocalDate]
)

case class Näyttö(
  suorituspaikka: Option[NäytönSuorituspaikka],
  suoritusaika: Option[schema.NäytönSuoritusaika],
  työssäoppimisenYhteydessä: Boolean,
  arviointi: Option[NäytönArviointi],
)

case class NäytönSuorituspaikka(
  tunniste: schema.Koodistokoodiviite,
  kuvaus: schema.LocalizedString
)

case class NäytönArviointi(
  hyväksytty: Boolean
)

case class Tutkinto(
  tunniste: schema.Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[schema.Koodistokoodiviite]
)

case class IBTheoryOfKnowledgeSuoritus(
  koulutusmoduuli: IBTheoryOfKnowledgeSuoritusKoulutusmoduuli,
  tila: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[Arviointi]] = None,
  osasuoritukset: Option[List[Osasuoritus]],
  tyyppi: schema.Koodistokoodiviite
)

case class IBTheoryOfKnowledgeSuoritusKoulutusmoduuli(
  tunniste: schema.Koodistokoodiviite,
  pakollinen: Boolean
)

case class IBExtendedEssaySuoritus(
  koulutusmoduuli: IBExtendedEssaySuoritusKoulutusmoduuli,
  tila: Option[schema.Koodistokoodiviite],
  arviointi: Option[List[Arviointi]] = None,
  tyyppi: schema.Koodistokoodiviite
)

case class IBExtendedEssaySuoritusKoulutusmoduuli(
  tunniste: schema.Koodistokoodiviite,
  pakollinen: Boolean
)

case class IBCASSuoritus(
  koulutusmoduuli: SuorituksenKoulutusmoduuli,
  arviointi: Option[List[Arviointi]] = None,
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite]
)

case class YlioppilastutkinnonTutkintokerta(
  koodiarvo: String,
  vuosi: Int,
  vuodenaika: schema.LocalizedString
)

case class VastaavuusTodistuksenTiedot(
  lukioOpintojenLaajuus: schema.Laajuus
)




