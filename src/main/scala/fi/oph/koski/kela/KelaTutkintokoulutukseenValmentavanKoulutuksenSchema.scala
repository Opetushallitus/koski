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
  järjestämislupa: schema.Koodistokoodiviite
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaTutkintokoulutukseenValmentavanOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
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
  erityisenTuenPäätökset: Option[List[KelaAikajakso]],
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
  tila: Option[KelaKoodistokoodiviite],
) extends Suoritus {
  def withEmptyArvosana: KelaTuvaPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Tutkintokoulutukseen valmentavan koulutuksen osasuoritus")
case class KelaTuvaOsasuoritus(
  koulutusmoduuli: KelaTuvaOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaTuvaOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaTuvaOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tunnustettu: Option[OsaamisenTunnustaminen],
  tila: Option[KelaKoodistokoodiviite],
) extends Osasuoritus {
  def withEmptyArvosana: KelaTuvaOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaTuvaOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArviointi {
  def withEmptyArvosana: KelaTuvaOsasuorituksenArvionti = copy(
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
