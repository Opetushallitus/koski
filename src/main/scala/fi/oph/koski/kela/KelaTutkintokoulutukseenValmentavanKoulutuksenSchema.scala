package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Tutkintokoulutukseen valmentavan koulutuksen opiskeluoikeus")
@Description("Tutkintokoulutukseen valmentava koulutus (TUVA)")
case class KelaTutkintokoulutukseenValmentavanOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaTuvaOpiskeluoikeudenTila,
  suoritukset: List[KelaTuvaPäätasonSuoritus],
  lisätiedot: Option[KelaTuvaOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.tuva.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
  järjestämislupa: KelaKoodistokoodiviite
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaTutkintokoulutukseenValmentavanOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaTuvaOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaTuvaOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KelaTuvaOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
  opintojenRahoitus: Option[KelaKoodistokoodiviite]
) extends Opiskeluoikeusjakso

@Title("Tutkintokoulutukseen valmentavan opiskeluoikeuden järjestämisluvan lisätiedot")
case class KelaTuvaOpiskeluoikeudenLisätiedot(
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]],
  majoitus: Option[List[KelaAikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityinenTuki: Option[List[KelaAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityisenTuenPäätökset: Option[List[KelaMahdollisestiAlkupäivätönAikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenErityinenTehtävä: Option[List[KelaAikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  osaAikaisuusjaksot: Option[List[KelaOsaAikaisuusJakso]],
  vankilaopetuksessa: Option[List[KelaAikajakso]],
  majoitusetu: Option[KelaAikajakso],
  koulukoti: Option[List[KelaAikajakso]],
) extends OpiskeluoikeudenLisätiedot

@Title("Tutkintokoulutukseen valmentavan koulutuksen suoritus")
case class KelaTuvaPäätasonSuoritus(
  toimipiste: Toimipiste,
  tyyppi: schema.Koodistokoodiviite,
  koulutusmoduuli: KelaTuvaSuorituksenKoulutusmoduuli,
  vahvistus: Option[Vahvistus],
  override val osasuoritukset: Option[List[KelaTuvaOsasuoritus]],
) extends Suoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaTuvaPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
case class KelaTuvaOsasuoritus(
  koulutusmoduuli: KelaTuvaOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaTuvaOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaTuvaOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[OsaamisenTunnustaminen],
) extends Osasuoritus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaTuvaOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

case class KelaTuvaOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaTuvaOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(
      schema.SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi.hyväksytty
    )
  )
}

case class KelaTuvaSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaTuvaOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
) extends OsasuorituksenKoulutusmoduuli
