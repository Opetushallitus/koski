package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("EB-tutkinnon opiskeluoikeus")
case class SupaEBOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: EBOpiskeluoikeudenTila,
  suoritukset: List[SupaEBTutkinnonSuoritus],
) extends SupaOpiskeluoikeus

object SupaEBOpiskeluoikeus {
  def apply(oo: EBOpiskeluoikeus, oppijaOid: String): SupaEBOpiskeluoikeus =
    SupaEBOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.map(SupaEBTutkinnonSuoritus.apply),
    )
}

@Title("EB-tutkinnon suoritus")
case class SupaEBTutkinnonSuoritus(
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: EBTutkinto,
  yleisarvosana: Option[Double],
  osasuoritukset: Option[List[SupaEBTutkinnonOsasuoritus]],
) extends SupaSuoritus with SupaVahvistuksellinen

object SupaEBTutkinnonSuoritus {
  def apply(pts: EBTutkinnonSuoritus): SupaEBTutkinnonSuoritus =
    SupaEBTutkinnonSuoritus(
      tyyppi = pts.tyyppi,
      alkamispäivä = pts.alkamispäivä,
      vahvistus = pts.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = pts.koulutusmoduuli,
      yleisarvosana = pts.yleisarvosana,
      osasuoritukset = pts.osasuoritukset.map(_.map(SupaEBTutkinnonOsasuoritus.apply)).filter(_.nonEmpty),
    )
}
@Title("EB-tutkinnon osasuoritus")
case class SupaEBTutkinnonOsasuoritus(
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: SecondaryOppiaine,
  osasuoritukset: Option[List[SupaEBOppiaineenFinalAlaosasuoritus]],
) extends SupaSuoritus

object SupaEBTutkinnonOsasuoritus {
  def apply (s: EBTutkinnonOsasuoritus): SupaEBTutkinnonOsasuoritus =
    SupaEBTutkinnonOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.map(_.flatMap(os => SupaEBOppiaineenFinalAlaosasuoritus(os))).filter(_.nonEmpty),
    )
}

@Title("EB-oppiaineen Final-alaosasuoritus")
case class SupaEBOppiaineenFinalAlaosasuoritus(
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: EBOppiaineKomponentti,
  arviointi: Option[List[Arviointi]],
) extends SupaSuoritus

object SupaEBOppiaineenFinalAlaosasuoritus {
  def apply(s: EBOppiaineenAlaosasuoritus): Option[SupaEBOppiaineenFinalAlaosasuoritus] =
    when (s.koulutusmoduuli.tunniste.koodiarvo == "Final") {
      SupaEBOppiaineenFinalAlaosasuoritus(
        tyyppi = s.tyyppi,
        koulutusmoduuli = s.koulutusmoduuli,
        arviointi = s.arviointi,
      )
    }
}
