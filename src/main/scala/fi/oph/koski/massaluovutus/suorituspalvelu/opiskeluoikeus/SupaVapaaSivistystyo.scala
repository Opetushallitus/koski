package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("Vapaan sivistystyön opiskeluoikeus")
case class SupaVapaanSivistystyönOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: VapaanSivistystyönOpiskeluoikeudenTila,
  suoritukset: List[SupaVapaanSivistystyönPäätasonSuoritus],
) extends SupaOpiskeluoikeus

trait SupaVapaanSivistystyönPäätasonSuoritus extends SupaSuoritus

object SupaVapaanSivistystyönOpiskeluoikeus {
  def apply(oo: VapaanSivistystyönOpiskeluoikeus, oppijaOid: String): SupaVapaanSivistystyönOpiskeluoikeus =
    SupaVapaanSivistystyönOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap {
        case s: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
          Some(SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(s))
        case _ =>
          None
      },
    )
}

@Title("Oppivelvollisille suunnattu vapaan sivistystyön koulutuksen suoritus")
case class SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
  @KoodistoKoodiarvo("vstoppivelvollisillesuunnattukoulutus")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus],
) extends SupaVapaanSivistystyönPäätasonSuoritus
  with Suorituskielellinen
  with SupaVahvistuksellinen

object SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus {
  def apply(s: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
    SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(os)
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(os)
      }
    )
}

trait SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus extends SupaSuoritus

@Title("Osaamiskokonaisuuden suoritus")
case class SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @KoodistoKoodiarvo("vstosaamiskokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus,
  osasuoritukset: List[SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus],
) extends SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus): SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
    SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map(SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus.apply)
    )
}

@Title("Opintokokonaisuuden suoritus")
case class SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
  @KoodistoKoodiarvo("vstopintokokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
) extends SupaVapaanSivistystyönOpintokokonaisuudenSuoritus

object SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus): SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
    SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}

@Title("Valinnaisten suuntautumisopintojen suoritus")
case class SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
  @KoodistoKoodiarvo("vstvalinnainensuuntautuminen")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot,
  osasuoritukset: List[SupaVapaanSivistystyönOpintokokonaisuudenSuoritus],
) extends SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus): SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
    SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.toList.flatten.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(os)
        case os: MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =>
          SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(os)
      }
    )
}

trait SupaVapaanSivistystyönOpintokokonaisuudenSuoritus extends SupaSuoritus

@Title("Muualla suoritettu opintojen suoritus")
case class SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
  @KoodistoKoodiarvo("vstmuuallasuoritetutopinnot")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot,
) extends SupaVapaanSivistystyönOpintokokonaisuudenSuoritus

object SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus {
  def apply(s: MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus): SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
    SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}
