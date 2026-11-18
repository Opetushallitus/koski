package fi.oph.koski.ehoks

import fi.oph.koski.schema
import fi.oph.koski.schema.LocalizedString
import fi.oph.scalaschema.annotation.{SyntheticProperty, Title}

import java.time.LocalDate

object EhoksSchema {
  val palautettavatOpiskeluoikeudenTyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo
  )
}

case class EhoksOppija(
  henkilö: EhoksHenkilö,
  opiskeluoikeudet: List[EhoksOpiskeluoikeus]
)

@Title("Henkilö")
case class EhoksHenkilö(oid: String)

@Title("Opiskeluoikeus")
case class EhoksOpiskeluoikeus(
  oid: String,
  tyyppi: EhoksKoodi,
  oppilaitos: Option[EhoksOppilaitos],
  koulutustoimija: Option[EhoksKoulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[EhoksSisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: EhoksOpiskeluoikeudenTila,
  suoritukset: List[EhoksPäätasonSuoritus]
) {
  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] =
    schema.Opiskeluoikeus.alkamispäivä(tyyppi.koodiarvo, tila.opiskeluoikeusjaksot.map(_.alku))

  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] =
    schema.Opiskeluoikeus.päättymispäivä(tyyppi.koodiarvo, tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)))
}

@Title("Koodi")
case class EhoksKoodi(koodiarvo: String)

@Title("Koodi nimellä")
case class EhoksNimettyKoodi(koodiarvo: String, nimi: Option[LocalizedString])

@Title("Organisaatio")
case class EhoksOrganisaatio(oid: String)

@Title("Oppilaitos")
case class EhoksOppilaitos(oid: String, nimi: Option[LocalizedString])

@Title("Koulutustoimija")
case class EhoksKoulutustoimija(oid: String, nimi: Option[LocalizedString])

@Title("Sisältävä opiskeluoikeus")
case class EhoksSisältäväOpiskeluoikeus(oid: String, oppilaitos: EhoksOrganisaatio)

@Title("Opiskeluoikeuden tila")
case class EhoksOpiskeluoikeudenTila(opiskeluoikeusjaksot: List[EhoksOpiskeluoikeusjakso])

@Title("Opiskeluoikeusjakso")
case class EhoksOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: EhoksKoodi,
  opintojenRahoitus: Option[EhoksKoodi]
)

@Title("Päätason suoritus")
case class EhoksPäätasonSuoritus(
  tyyppi: EhoksKoodi,
  koulutusmoduuli: EhoksKoulutusmoduuli,
  osaamisala: Option[List[EhoksOsaamisalajakso]],
  tutkintonimike: Option[List[EhoksNimettyKoodi]],
  suorituskieli: Option[EhoksKoodi],
  toimipiste: Option[EhoksOrganisaatio],
  vahvistus: Option[EhoksVahvistus]
)

@Title("Koulutusmoduuli")
case class EhoksKoulutusmoduuli(tunniste: EhoksNimettyKoodi, perusteenDiaarinumero: Option[String])

@Title("Osaamisalajakso")
case class EhoksOsaamisalajakso(osaamisala: EhoksNimettyKoodi, alku: Option[LocalDate], loppu: Option[LocalDate])

@Title("Vahvistus")
case class EhoksVahvistus(päivä: LocalDate)
