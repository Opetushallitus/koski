package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.{LocalizedString, OpiskeluoikeudenTyyppi}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Aikuisten perusopetuksen opiskeluoikeus")
@Description("Aikuisten perusopetuksen opiskeluoikeus")
case class KelaAikuistenPerusopetuksenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaAikuistenPerusopetuksenSuoritus],
  lisätiedot: Option[KelaAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def arvioituPäättymispäivä = None
  def withEmptyArvosana: KelaAikuistenPerusopetuksenOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
  sisäoppilaitosmainenMajoitus: Option[List[schema.Aikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  majoitusetu: Option[schema.Aikajakso],
  ulkomailla: Option[schema.Aikajakso],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätös: Option[schema.TehostetunTuenPäätös],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätökset: Option[List[schema.TehostetunTuenPäätös]],
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Aikuisten perusopetuksen suoritus")
case class KelaAikuistenPerusopetuksenPäätasonSuoritus(
  koulutusmoduuli: KelaAikuistenPerusopetuksenSuorituksenKoulutusmoduuli,
  suoritustapa: Option[KelaKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAikuistenPerusopetuksenOsasuoritus]],
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaaranalkuvaihe")
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends KelaAikuistenPerusopetuksenSuoritus {
  def withEmptyArvosana: KelaAikuistenPerusopetuksenPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Aikuisten perusopetuksen oppiaineen oppimäärän suoritus")
case class KelaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
  koulutusmoduuli: KelaAikuistenPerusopetuksenSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAikuistenPerusopetuksenOsasuoritus]],
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
) extends KelaAikuistenPerusopetuksenSuoritus {
  def withEmptyArvosana: KelaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

trait KelaAikuistenPerusopetuksenSuoritus extends Suoritus {
  def withEmptyArvosana: KelaAikuistenPerusopetuksenSuoritus
}

@Title("Aikuisten perusopetuksen osasuoritus")
case class KelaAikuistenPerusopetuksenOsasuoritus(
  koulutusmoduuli: KelaAikuistenPerusopetuksenOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaPerusopetuksenOsasuorituksenArviointi]],
  osasuoritukset: Option[List[KelaAikuistenPerusopetuksenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
) extends Osasuoritus {
  def withEmptyArvosana: KelaAikuistenPerusopetuksenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaAikuistenPerusopetuksenSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaAikuistenPerusopetuksenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli
