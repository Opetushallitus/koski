package fi.oph.koski.suoritetuttutkinnot

import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri}
import fi.oph.scalaschema.annotation.Discriminator

import java.time.LocalDate

object SuoritetutTutkinnotSchema {
  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
  )
}

case class SuoritetutTutkinnotOppija(
  henkilö: LaajatOppijaHenkilöTiedot,
  opiskeluoikeudet: List[SuoritetutTutkinnotOpiskeluoikeus]
)

trait SuoritetutTutkinnotOpiskeluoikeus {
  def oppilaitos: Option[Oppilaitos]
  def koulutustoimija: Option[Koulutustoimija]
  def suoritukset: List[Suoritus]
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus
  def withoutSisältyyOpiskeluoikeuteen: SuoritetutTutkinnotOpiskeluoikeus
}

trait SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus extends SuoritetutTutkinnotOpiskeluoikeus {
  def oid: Option[String]
  def versionumero: Option[Int]
  @Deprecated("Ei palauteta. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
}

case class SisältäväOpiskeluoikeus(
  oid: String,
  oppilaitos: Oppilaitos
)

trait Suoritus{
  def koulutusmoduuli: SuorituksenKoulutusmoduuli
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite
  def vahvistus: Option[Vahvistus]
}

trait SuorituksenKoulutusmoduuli {
  def tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

case class SuoritetutTutkinnotKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
)
object SuoritetutTutkinnotKoodistokoodiviite{
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = SuoritetutTutkinnotKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

case class Vahvistus(päivä: LocalDate)

case class Oppilaitos(
  oid: String,
  oppilaitosnumero: Option[SuoritetutTutkinnotKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite]
)

case class Koulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite]
)

case class Toimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[SuoritetutTutkinnotKoodistokoodiviite] = None
)

case class SuoritetutTutkinnotLaajuus(arvo: Double, yksikkö: SuoritetutTutkinnotKoodistokoodiviite)
