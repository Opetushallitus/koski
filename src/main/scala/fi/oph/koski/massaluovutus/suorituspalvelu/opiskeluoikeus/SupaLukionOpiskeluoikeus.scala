package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Lukion opiskeluoikeus")
case class SupaLukionOpiskeluoikeus(
  oppijaOid: String,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: LukionOpiskeluoikeudenTila,
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppimääräSuoritettu: Option[Boolean],
  suoritukset: List[SupaLukionPäätasonSuoritus],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
) extends SupaOpiskeluoikeus

object SupaLukionOpiskeluoikeus {
  def apply(oo: LukionOpiskeluoikeus, oppijaOid: String): SupaLukionOpiskeluoikeus =
    SupaLukionOpiskeluoikeus(
      oppijaOid = oppijaOid,
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      alkamispäivä = oo.alkamispäivä,
      päättymispäivä = oo.päättymispäivä,
      oppimääräSuoritettu = oo.oppimääräSuoritettu,
      suoritukset = oo.suoritukset.collect {
        case s: LukionOppimääränSuoritus2019 => SupaLukionOppimääränSuoritus2019(s)
        case s: LukionOppimääränSuoritus2015 => SupaLukionOppimääränSuoritus2015(s)
        case s: LukionOppiaineidenOppimäärienSuoritus2019 => SupaLukionOppiaineidenOppimäärienSuoritus2019(s)
        case s: LukionOppiaineenOppimääränSuoritus2015 => SupaLukionOppiaineenOppimääränSuoritus2015(s)
      },
      versionumero = oo.versionumero,
      aikaleima = oo.aikaleima
    )
}
trait SupaLukionPäätasonSuoritus extends SupaSuoritus

@Title("Lukion oppimäärän suoritus 2019")
case class SupaLukionOppimääränSuoritus2019(
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: LukionOppimäärä,
  oppimäärä: Koodistokoodiviite,
  vahvistus: Option[SupaVahvistus],
  koulusivistyskieli: Option[List[Koodistokoodiviite]],
) extends SupaLukionPäätasonSuoritus
  with SupaVahvistuksellinen

object SupaLukionOppimääränSuoritus2019 {
  def apply(s: LukionOppimääränSuoritus2019): SupaLukionOppimääränSuoritus2019 =
    SupaLukionOppimääränSuoritus2019(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      oppimäärä = s.oppimäärä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulusivistyskieli = s.koulusivistyskieli,
    )
}

@Title("Lukion oppimäärän suoritus")
case class SupaLukionOppimääränSuoritus2015(
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: LukionOppimäärä,
  oppimäärä: Koodistokoodiviite,
  vahvistus: Option[SupaVahvistus],
  koulusivistyskieli: Option[List[Koodistokoodiviite]],
) extends SupaLukionPäätasonSuoritus
  with SupaVahvistuksellinen

object SupaLukionOppimääränSuoritus2015 {
  def apply(s: LukionOppimääränSuoritus2015): SupaLukionOppimääränSuoritus2015 =
    SupaLukionOppimääränSuoritus2015(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      oppimäärä = s.oppimäärä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulusivistyskieli = s.koulusivistyskieli,
    )
}

@Title("Lukion oppiaineiden oppimäärien suoritus 2019")
case class SupaLukionOppiaineidenOppimäärienSuoritus2019(
  @KoodistoKoodiarvo("lukionaineopinnot")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019,
  oppimäärä: Koodistokoodiviite,
  vahvistus: Option[SupaVahvistus],
) extends SupaLukionPäätasonSuoritus
  with SupaVahvistuksellinen

object SupaLukionOppiaineidenOppimäärienSuoritus2019 {
  def apply(s: LukionOppiaineidenOppimäärienSuoritus2019): SupaLukionOppiaineidenOppimäärienSuoritus2019 =
    SupaLukionOppiaineidenOppimäärienSuoritus2019(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
      oppimäärä = s.oppimäärä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
    )
}

@Title("Lukion oppiaineen oppimäärän suoritus")
case class SupaLukionOppiaineenOppimääränSuoritus2015(
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite,
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine2015,
) extends SupaLukionPäätasonSuoritus

object SupaLukionOppiaineenOppimääränSuoritus2015 {
  def apply(s: LukionOppiaineenOppimääränSuoritus2015): SupaLukionOppiaineenOppimääränSuoritus2015 =
    SupaLukionOppiaineenOppimääränSuoritus2015(
      tyyppi = s.tyyppi,
      koulutusmoduuli = s.koulutusmoduuli,
    )
}
