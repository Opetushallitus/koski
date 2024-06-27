package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Vapaa sivistystyö")
case class SureVapaanSivistystyönOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SureVapaanSivistystyönPäätasonSuoritus],
) extends SureOpiskeluoikeus

trait SureVapaanSivistystyönPäätasonSuoritus extends SureSuoritus

object SureVapaanSivistystyönOpiskeluoikeus {
  def apply(oo: VapaanSivistystyönOpiskeluoikeus): SureVapaanSivistystyönOpiskeluoikeus =
    SureVapaanSivistystyönOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap {
        case s: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
          Some(SureOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(s))
        case _ =>
          None
      },
    )
}

@Title("Oppivelvollisille suunnattu vapaan sivistystyön koulutuksen suoritus")
case class SureOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SureOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus],
) extends SureVapaanSivistystyönPäätasonSuoritus
  with Suorituskielellinen
  with Vahvistuspäivällinen

object SureOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus {
  def apply(s: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): SureOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
    SureOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =>
          SureOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(os)
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =>
          SureOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(os)
      }
    )
}

trait SureOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus extends SureSuoritus

@Title("Osaamiskokonaisuuden suoritus")
case class SureOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @KoodistoKoodiarvo("vstosaamiskokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus,
  osasuoritukset: List[SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus],
) extends SureOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SureOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus): SureOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
    SureOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus.apply)
    )
}

@Title("Opintokokonaisuuden suoritus")
case class SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @KoodistoKoodiarvo("vstopintokokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
) extends SureVapaanSivistystyönOpintokokonaisuudenSuoritus

object SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus): SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
    SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}

@Title("Valinnaisten suuntautumisopintojen suoritus")
case class SureOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
  @KoodistoKoodiarvo("vstvalinnainensuuntautuminen")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot,
  osasuoritukset: List[SureVapaanSivistystyönOpintokokonaisuudenSuoritus],
) extends SureOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SureOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus): SureOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
    SureOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =>
          SureOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(os)
        case os: MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =>
          SureMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(os)
      }
    )
}

trait SureVapaanSivistystyönOpintokokonaisuudenSuoritus extends SureSuoritus

@Title("Muualla suoritettu opintojen suoritus")
case class SureMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
  @KoodistoKoodiarvo("vstmuuallasuoritetutopinnot")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot,
) extends SureVapaanSivistystyönOpintokokonaisuudenSuoritus

object SureMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus {
  def apply(s: MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus): SureMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
    SureMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}
