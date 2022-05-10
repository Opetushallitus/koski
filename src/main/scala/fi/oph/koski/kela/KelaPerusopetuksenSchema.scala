package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Discriminator, OnlyWhen, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Perusopetuksen opiskeluoikeus")
@Description("Perusopetuksen opiskeluoikeus")
case class KelaPerusopetuksenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaPerusopetuksenPäätasonSuoritus],
  lisätiedot: Option[KelaPerusopetuksenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def arvioituPäättymispäivä = None
  def withEmptyArvosana: KelaPerusopetuksenOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
}

case class KelaPerusopetuksenOpiskeluoikeudenLisätiedot(
  sisäoppilaitosmainenMajoitus: Option[List[schema.Aikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  koulukoti: Option[List[schema.Aikajakso]],
  majoitusetu: Option[schema.Aikajakso],
  ulkomailla: Option[schema.Aikajakso],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätös: Option[schema.TehostetunTuenPäätös],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  tehostetunTuenPäätökset: Option[List[schema.TehostetunTuenPäätös]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  joustavaPerusopetus: Option[schema.Aikajakso],
) extends OpiskeluoikeudenLisätiedot

@Title("Perusopetuksen suoritus")
case class KelaPerusopetuksenPäätasonMuuSuoritus(
  koulutusmoduuli: KelaPerusopetuksenSuorituksenKoulutusmoduuli,
  suoritustapa: Option[KelaKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaPerusopetuksenOsasuoritus]],
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  alkamispäivä: Option[LocalDate],
  jääLuokalle: Option[Boolean]
) extends KelaPerusopetuksenPäätasonSuoritus {
  def withEmptyArvosana: KelaPerusopetuksenPäätasonMuuSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Perusopetuksen oppiaineen oppimäärän suoritus")
case class KelaPerusopetuksenOppiaineenOppimääränSuoritus(
  koulutusmoduuli: KelaPerusopetuksenSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaPerusopetuksenOsasuoritus]],
  @KoodistoKoodiarvo("nuortenperusopetuksenoppiaineenoppimaara")
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  alkamispäivä: Option[LocalDate],
  jääLuokalle: Option[Boolean]
) extends KelaPerusopetuksenPäätasonSuoritus {
  def withEmptyArvosana: KelaPerusopetuksenOppiaineenOppimääränSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

trait KelaPerusopetuksenPäätasonSuoritus extends Suoritus {
  def withEmptyArvosana: KelaPerusopetuksenPäätasonSuoritus
}

@Title("Perusopetuksen osasuoritus")
case class KelaPerusopetuksenOsasuoritus(
  koulutusmoduuli: KelaPerusopetuksenOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaPerusopetuksenOsasuorituksenArvionti]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Option[Boolean]
) extends Osasuoritus with YksilöllistettyOppimäärä {
  def withEmptyArvosana: KelaPerusopetuksenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaPerusopetuksenOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaPerusopetuksenOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.YleissivistävänKoulutuksenArviointi.hyväksytty)
  )
}

case class KelaPerusopetuksenSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[schema.Laajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  pakollinen: Option[Boolean],
  kieli: Option[schema.Koodistokoodiviite]
) extends SuorituksenKoulutusmoduuli

case class KelaPerusopetuksenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[schema.Laajuus],
  pakollinen: Option[Boolean],
  kieli: Option[schema.Koodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli
