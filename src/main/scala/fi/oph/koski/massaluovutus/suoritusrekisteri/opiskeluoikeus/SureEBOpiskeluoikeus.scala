package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.Optional.when
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

@Title("EB-tutkinto")
case class SureEBOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SureEBTutkinnonSuoritus],
) extends SureOpiskeluoikeus

object SureEBOpiskeluoikeus {
  def apply(oo: EBOpiskeluoikeus): SureEBOpiskeluoikeus =
    SureEBOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.map(SureEBTutkinnonSuoritus.apply),
    )
}

@Title("EB-tutkinnon suoritus")
case class SureEBTutkinnonSuoritus(
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: EBTutkinto,
  yleisarvosana: Option[Double],
  osasuoritukset: Option[List[SureEBTutkinnonOsasuoritus]],
) extends SureSuoritus

object SureEBTutkinnonSuoritus {
  def apply(pts: EBTutkinnonSuoritus): SureEBTutkinnonSuoritus =
    SureEBTutkinnonSuoritus(
      tyyppi = pts.tyyppi,
      alkamispäivä = pts.alkamispäivä,
      vahvistuspäivä = pts.vahvistus.map(_.päivä),
      koulutusmoduuli = pts.koulutusmoduuli,
      yleisarvosana = pts.yleisarvosana,
      osasuoritukset = pts.osasuoritukset.map(_.map(SureEBTutkinnonOsasuoritus.apply)),
    )
}
@Title("EB-tutkinnon osasuoritus")
case class SureEBTutkinnonOsasuoritus(
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: SecondaryOppiaine,
  osasuoritukset: List[SureEBOppiaineenFinalAlaosasuoritus],
) extends SureSuoritus

object SureEBTutkinnonOsasuoritus {
  def apply (s: EBTutkinnonOsasuoritus): SureEBTutkinnonOsasuoritus =
    SureEBTutkinnonOsasuoritus(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      osasuoritukset = s.osasuoritukset.toList.flatten.flatMap(os => SureEBOppiaineenFinalAlaosasuoritus(os)),
    )
}

@Title("EB-oppiaineen Final-alaosasuoritus")
case class SureEBOppiaineenFinalAlaosasuoritus(
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: EBOppiaineKomponentti,
  arviointi: Option[List[Arviointi]],
) extends SureSuoritus

object SureEBOppiaineenFinalAlaosasuoritus {
  def apply(s: EBOppiaineenAlaosasuoritus): Option[SureEBOppiaineenFinalAlaosasuoritus] =
    when (s.koulutusmoduuli.tunniste.koodiarvo == "Final") {
      SureEBOppiaineenFinalAlaosasuoritus(
        tyyppi = s.tyyppi,
        koulutusmoduuli = s.koulutusmoduuli,
        arviointi = s.arviointi,
      )
    }
}
