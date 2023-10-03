package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{ComplexObject, Hidden, KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.{Description, SyntheticProperty, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Lukion opiskeluoikeus")
@Description("Lukion opiskeluoikeus")
case class KelaLukionOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTilaRahoitustiedoilla,
  suoritukset: List[KelaLukionPäätasonSuoritus],
  lisätiedot: Option[KelaLukionOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaLukionOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaLukionOpiskeluoikeudenLisätiedot(
  sisäoppilaitosmainenMajoitus: Option[List[KelaAikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  ulkomainenVaihtoopiskelija: Option[Boolean],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Lukion suoritus")
case class KelaLukionPäätasonSuoritus(
  koulutusmoduuli: KelaLukionSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  oppimäärä: Option[KelaKoodistokoodiviite],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaLukionOsasuoritus]],
  omanÄidinkielenOpinnot: Option[KelaLukionOmanÄidinkielenOpinnot] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite]
) extends Suoritus {
  def withEmptyArvosana: KelaLukionPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
    omanÄidinkielenOpinnot = omanÄidinkielenOpinnot.map(_.withEmptyArvosana),
  )
}

case class KelaLukionOmanÄidinkielenOpinnot(
  arvosana: Option[Koodistokoodiviite],
  arviointipäivä: Option[LocalDate],
  laajuus: LaajuusOpintopisteissä,
  osasuoritukset: Option[List[KelaLukionOmanÄidinkielenOpintojenOsasuoritus]],
  hyväksytty: Option[Boolean],
) {
  def withEmptyArvosana: KelaLukionOmanÄidinkielenOpinnot = copy(
    arvosana = None,
    hyväksytty = arvosana.map(YleissivistävänKoulutuksenArviointi.hyväksytty),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}

case class KelaLukionOmanÄidinkielenOpintojenOsasuoritus(
  @Hidden
  tyyppi: Koodistokoodiviite,
  @Title("Kurssi")
  koulutusmoduuli: LukionOmanÄidinkielenOpinto,
  arviointi: Option[List[KelaLukionOsasuorituksenArvionti]] = None,
  @ComplexObject
  @Hidden
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
) extends Osasuoritus {
  def description: LocalizedString = koulutusmoduuli.nimi
  def nimi: LocalizedString = koulutusmoduuli.nimi

  def withEmptyArvosana: KelaLukionOmanÄidinkielenOpintojenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
  )
}

@Title("Lukion osasuoritus")
case class KelaLukionOsasuoritus(
  koulutusmoduuli: KelaLukionOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaLukionOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaLukionOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tunnustettu: Option[OsaamisenTunnustaminen],
  suoritettuLukiodiplomina: Option[Boolean],
  suoritettuSuullisenaKielikokeena: Option[Boolean]
) extends Osasuoritus {
  def withEmptyArvosana: KelaLukionOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaLukionOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaLukionOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.YleissivistävänKoulutuksenArviointi.hyväksytty)
  )
}

case class KelaLukionSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
  oppimäärä: Option[KelaKoodistokoodiviite]
) extends SuorituksenKoulutusmoduuli

case class KelaLukionOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
  kurssinTyyppi: Option[KelaKoodistokoodiviite],
  oppimäärä: Option[KelaKoodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli
