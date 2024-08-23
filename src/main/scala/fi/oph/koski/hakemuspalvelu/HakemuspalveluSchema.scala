package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoUri, Representative}
import fi.oph.scalaschema.annotation.{Discriminator, SyntheticProperty, Title}
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

object HakemuspalveluSchema {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[HakemuspalveluOppija]).asInstanceOf[ClassSchema])

  val schemassaTuetutOpiskeluoikeustyypit: List[String] = List(
    schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo,
    schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo
  )
}

case class HakemuspalveluOppija(
  henkilö: HakemuspalveluHenkilo,
  opiskeluoikeudet: List[HakemuspalveluOpiskeluoikeus]
)

case class HakemuspalveluHenkilo(
  oid: String,
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)

object HakemuspalveluHenkilo {
  def fromOppijaHenkilö(oppijaHenkilö: fi.oph.koski.henkilo.OppijaHenkilö) = HakemuspalveluHenkilo(
    oid = oppijaHenkilö.oid,
    syntymäaika = oppijaHenkilö.syntymäaika,
    etunimet = oppijaHenkilö.etunimet,
    sukunimi = oppijaHenkilö.sukunimi,
    kutsumanimi = oppijaHenkilö.kutsumanimi
  )
}

trait HakemuspalveluOpiskeluoikeus {
  def oppilaitos: Option[HakemuspalveluOppilaitos]

  def koulutustoimija: Option[HakemuspalveluKoulutustoimija]

  def suoritukset: List[HakemuspalveluSuoritus]

  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = schema.Opiskeluoikeus.alkamispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(_.alku))

  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] = schema.Opiskeluoikeus.päättymispäivä(this.tyyppi.koodiarvo, this.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)))

  def tila: HakemuspalveluOpiskeluoikeudenTila

  def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot]

  def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluOpiskeluoikeus
}

trait HakemuspalveluKoskeenTallennettavaOpiskeluoikeus extends HakemuspalveluOpiskeluoikeus {
  def oid: Option[String]

  def versionumero: Option[Int]
}

case class HakemuspalveluSisältäväOpiskeluoikeus(
  oid: String,
  oppilaitos: HakemuspalveluOppilaitos
)

trait HakemuspalveluSuoritus {
  def koulutusmoduuli: HakemuspalveluSuorituksenKoulutusmoduuli

  @Discriminator
  def tyyppi: schema.Koodistokoodiviite

  def vahvistus: Option[HakemuspalveluVahvistus]

  def toimipiste: Option[HakemuspalveluToimipiste]
}

trait HakemuspalveluKoodiViite {
  def koodiarvo: String
}

@Title("Koodistokoodiviite")
case class HakemuspalveluKoodistokoodiviite(
  koodiarvo: String,
  nimi: Option[schema.LocalizedString],
  lyhytNimi: Option[schema.LocalizedString],
  koodistoUri: Option[String],
  koodistoVersio: Option[Int]
) extends HakemuspalveluKoodiViite

object HakemuspalveluKoodistokoodiviite {
  def fromKoskiSchema(kv: schema.Koodistokoodiviite) = HakemuspalveluKoodistokoodiviite(
    kv.koodiarvo,
    kv.nimi,
    kv.lyhytNimi,
    Some(kv.koodistoUri),
    kv.koodistoVersio
  )
}

@Title("Paikallinen koodi")
case class HakemuspalveluPaikallinenKoodi(
  koodiarvo: String,
  nimi: schema.LocalizedString,
  koodistoUri: Option[String]
) extends HakemuspalveluKoodiViite

case class HakemuspalveluOppilaitos(
  oid: String,
  oppilaitosnumero: Option[HakemuspalveluKoodistokoodiviite],
  nimi: Option[schema.LocalizedString],
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite]
)

case class HakemuspalveluKoulutustoimija(
  oid: String,
  nimi: Option[schema.LocalizedString],
  yTunnus: Option[String],
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite]
)

@Title("Opiskeluoikeuden tila")
case class HakemuspalveluOpiskeluoikeudenTila(
  @Representative
  opiskeluoikeusjaksot: List[HakemuspalveluOpiskeluoikeusjakso]
)

@Title("Opiskeluoikeusjakso")
case class HakemuspalveluOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: HakemuspalveluKoodistokoodiviite,
  opintojenRahoitus: Option[HakemuspalveluKoodistokoodiviite]
)

@Title("Opiskeluoikeuden lisätiedot")
trait HakemuspalveluOpiskeluoikeudenLisätiedot

trait HakemuspalveluSuorituksenKoulutusmoduuli {
  def tunniste: HakemuspalveluKoodiViite
}

trait HakemuspalveluSuorituksenKooditettuKoulutusmoduuli extends HakemuspalveluSuorituksenKoulutusmoduuli {
  def tunniste: HakemuspalveluKoodistokoodiviite
}

case class HakemuspalveluVahvistus(päivä: LocalDate)

case class HakemuspalveluToimipiste(
  oid: String,
  nimi: Option[schema.LocalizedString] = None,
  kotipaikka: Option[HakemuspalveluKoodistokoodiviite] = None
)
