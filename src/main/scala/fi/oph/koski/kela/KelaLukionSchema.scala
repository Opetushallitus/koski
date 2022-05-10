package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.{LocalizedString, OpiskeluoikeudenTyyppi}
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Lukion opiskeluoikeus")
@Description("Lukion opiskeluoikeus")
case class KelaLukionOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaLukionPäätasonSuoritus],
  lisätiedot: Option[KelaLukionOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaLukionOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
}

case class KelaLukionOpiskeluoikeudenLisätiedot(
  sisäoppilaitosmainenMajoitus: Option[List[schema.Aikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  ulkomainenVaihtoopiskelija: Option[Boolean],
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Lukion suoritus")
case class KelaLukionPäätasonSuoritus(
  koulutusmoduuli: KelaLukionSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  oppimäärä: Option[schema.Koodistokoodiviite],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaLukionOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite]
) extends Suoritus {
  def withEmptyArvosana = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Lukion osasuoritus")
case class KelaLukionOsasuoritus(
  koulutusmoduuli: KelaLukionOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaLukionOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaLukionOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[schema.Koodistokoodiviite],
  tunnustettu: Option[KelaLukionOsaamisenTunnustaminen],
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

case class KelaLukionOsaamisenTunnustaminen(
  osaaminen: Option[KelaLukionOsasuoritus],
  selite: LocalizedString,
  rahoituksenPiirissä: Boolean
) extends OsaamisenTunnustaminen

case class KelaLukionSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[schema.Laajuus],
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  pakollinen: Option[Boolean],
  kieli: Option[schema.Koodistokoodiviite],
  oppimäärä: Option[schema.Koodistokoodiviite]
) extends SuorituksenKoulutusmoduuli

case class KelaLukionOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[schema.Laajuus],
  pakollinen: Option[Boolean],
  kieli: Option[schema.Koodistokoodiviite],
  kurssinTyyppi: Option[schema.Koodistokoodiviite],
  oppimäärä: Option[schema.Koodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli
