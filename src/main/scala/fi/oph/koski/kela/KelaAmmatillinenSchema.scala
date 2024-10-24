package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Ammatillisen koulutuksen opiskeluoikeus")
@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class KelaAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  @DefaultValue(false)
  ostettu: Boolean = false,
  tila: KelaOpiskeluoikeudenTilaRahoitustiedoilla,
  suoritukset: List[KelaAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[KelaAmmatillisenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaAmmatillinenOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaAmmatillisenOpiskeluoikeudenLisätiedot(
  majoitus: Option[List[KelaAikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityinenTuki: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenErityinenTehtävä: Option[List[KelaAikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  hojks: Option[Hojks],
  osaAikaisuusjaksot: Option[List[KelaOsaAikaisuusJakso]],
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[KelaOpiskeluvalmiuksiaTukevienOpintojenJakso]],
  vankilaopetuksessa: Option[List[KelaAikajakso]],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]],
  koulutusvienti: Option[Boolean],
) extends OpiskeluoikeudenLisätiedot

@Title("Ammatillisen koulutuksen suoritus")
case class KelaAmmatillinenPäätasonSuoritus(
  koulutusmoduuli: KelaAmmatillisenSuorituksenKoulutusmoduuli,
  suoritustapa: Option[KelaKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAmmatillinenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  osaamisala: Option[List[KelaOsaamisalajakso]],
  toinenOsaamisala: Option[Boolean],
  alkamispäivä: Option[LocalDate],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  tutkintonimike: Option[List[KelaKoodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
  täydentääTutkintoa: Option[Tutkinto],     // Muu ammatillinen
  tutkinto: Option[Tutkinto],               // Näyttötutkintoon valmistava
  päättymispäivä: Option[LocalDate]         // Näyttötutkintoon valmistava
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Ammatillisen koulutuksen osasuoritus")
case class KelaAmmatillinenOsasuoritus(
  koulutusmoduuli: KelaAmmatillisenOsasuorituksenKoulutusmoduuli,
  liittyyTutkinnonOsaan: Option[KelaKoodistokoodiviite],
  liittyyTutkintoon: Option[Tutkinto],
  arviointi: Option[List[KelaAmmatillisenOsasuorituksenArviointi]],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAmmatillinenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tutkinto: Option[Tutkinto],
  tutkinnonOsanRyhmä: Option[KelaKoodistokoodiviite],
  osaamisala: Option[List[KelaOsaamisalajakso]],
  alkamispäivä: Option[LocalDate],
  tunnustettu: Option[OsaamisenTunnustaminen],
  toinenOsaamisala: Option[Boolean],
  toinenTutkintonimike: Option[Boolean],
  näyttö: Option[Näyttö],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]],
  korotettu: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaAmmatillinenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    näyttö = näyttö.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
}

case class KelaAmmatillisenOsasuorituksenArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaAmmatillisenOsasuorituksenArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.AmmatillinenKoodistostaLöytyväArviointi.hyväksytty)
  )
}

case class KelaAmmatillisenSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  pakollinen: Option[Boolean],
  kuvaus: Option[schema.LocalizedString],
  kieli: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaAmmatillisenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenNimi: Option[schema.LocalizedString],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli

case class Hojks(
  opetusryhmä: KelaKoodistokoodiviite,
  alku: Option[LocalDate],
  loppu: Option[LocalDate]
)

case class Näyttö(
  suorituspaikka: Option[NäytönSuorituspaikka],
  suoritusaika: Option[NäytönSuoritusaika],
  työssäoppimisenYhteydessä: Boolean,
  arviointi: Option[NäytönArviointi],
) {
  def withHyväksyntämerkinnälläKorvattuArvosana: Näyttö = copy(arviointi = arviointi.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
}

case class NäytönSuorituspaikka(
  tunniste: KelaKoodistokoodiviite,
  kuvaus: schema.LocalizedString
)

case class NäytönSuoritusaika(
  alku: LocalDate,
  loppu: LocalDate
)

case class NäytönArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: LocalDate,
) extends SisältääHyväksyntämerkinnälläKorvatunArvosanan {
  def withHyväksyntämerkinnälläKorvattuArvosana: NäytönArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.AmmatillinenKoodistostaLöytyväArviointi.hyväksytty)
  )
}

case class Tutkinto(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[KelaKoodistokoodiviite]
)

case class AmmatillisenTutkinnonOsanLisätieto(
  @KoodistoKoodiarvo("mukautettu")
  tunniste: schema.Koodistokoodiviite,
  kuvaus: schema.LocalizedString
)

trait OsaamisenHankkimistapa {
  def tunniste: KelaKoodistokoodiviite
}

case class OsaamisenHankkimistapaIlmanLisätietoja (
  tunniste: KelaKoodistokoodiviite
) extends OsaamisenHankkimistapa

case class OppisopimuksellinenOsaamisenHankkimistapa (
  tunniste: KelaKoodistokoodiviite,
  oppisopimus: Oppisopimus
) extends OsaamisenHankkimistapa

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  osaamisenHankkimistapa: OsaamisenHankkimistapa
)

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: KelaKoodistokoodiviite,
  maa: KelaKoodistokoodiviite,
  laajuus: KelaLaajuus
)

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[schema.LocalizedString],
  paikkakunta: KelaKoodistokoodiviite,
  maa: KelaKoodistokoodiviite,
  @Title("Työssäoppimispaikan Y-tunnus")
  työssäoppimispaikanYTunnus: Option[String],
)

case class Järjestämismuoto (
  tunniste: KelaKoodistokoodiviite
)

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  järjestämismuoto: Järjestämismuoto
)

case class Oppisopimus(
  työnantaja: Yritys,
  oppisopimuksenPurkaminen: Option[OppisopimuksenPurkaminen]
)

case class OppisopimuksenPurkaminen(
  päivä: LocalDate,
  purettuKoeajalla: Boolean
)

case class Yritys(
  nimi: schema.LocalizedString,
  yTunnus: String
)
