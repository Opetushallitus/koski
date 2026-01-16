package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Vapaan sivistystyön opiskeluoikeus")
case class SupaVapaanSivistystyönOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: VapaanSivistystyönOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  suoritukset: List[SupaVapaanSivistystyönPäätasonSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
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
      alkamispäivä = oo.alkamispäivä,
      päättymispäivä = oo.päättymispäivä,
      suoritukset = oo.suoritukset.flatMap {
        case s: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
          Some(SupaOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(s))
        case s: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus =>
          Some(SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(s))
        case _ =>
          None
      },
      versionumero = oo.versionumero,
      aikaleima = oo.aikaleima
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
  osasuoritukset: Option[List[SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus]],
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
      osasuoritukset = s.osasuoritukset.map(_.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(os)
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(os)
      }).filter(_.nonEmpty)
    )
}

trait SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus extends SupaSuoritus

@Title("Osaamiskokonaisuuden suoritus")
case class SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
  @KoodistoKoodiarvo("vstosaamiskokonaisuus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus,
  osasuoritukset: Option[List[SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]],
) extends SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus): SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
    SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map(_.map(SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus.apply)).filter(_.nonEmpty)
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
  osasuoritukset: Option[List[SupaVapaanSivistystyönOpintokokonaisuudenSuoritus]],
) extends SupaOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus

object SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus {
  def apply(s: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus): SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
    SupaOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map(_.map {
        case os: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =>
          SupaOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(os)
        case os: MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =>
          SupaMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(os)
      }).filter(_.nonEmpty)
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

@Title("Vapaatavoitteisen vapaan sivistystyön koulutuksen suoritus")
case class SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
  @KoodistoKoodiarvo("vstvapaatavoitteinenkoulutus")
  tyyppi: Koodistokoodiviite,
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus,
  osasuoritukset: Option[List[SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus]]
) extends SupaVapaanSivistystyönPäätasonSuoritus with SupaVahvistuksellinen

object SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus {
  def apply(s: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus): SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus =
    SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
      tyyppi = s.tyyppi,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map(_.map(SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.apply)).filter(_.nonEmpty)
    )
}

@Title("Vapaatavoitteisen vapaan sivistystyön koulutuksen osasuorituksen suoritus")
case class SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
  @KoodistoKoodiarvo("vstvapaatavoitteisenkoulutuksenosasuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus,
  arviointi: Option[List[Arviointi]],
  osasuoritukset: Option[List[SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus]]
) extends SupaSuoritus

object SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus {
  def apply(os: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus): SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
    SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
      tyyppi = os.tyyppi,
      koulutusmoduuli = os.koulutusmoduuli,
      arviointi = os.arviointi,
      osasuoritukset = os.osasuoritukset.map(_.map(SupaVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.apply)).filter(_.nonEmpty)
    )
}
