package fi.oph.koski.schema

import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.schema.LocalizedString.unlocalized

import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, Discriminator, OnlyWhen, SyntheticProperty, Title}
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.annotation.Deprecated

case class KorkeakoulunOpiskeluoikeus(
  oid: Option[String] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  override val päättymispäivä: Option[LocalDate] = None,
  @Description("Jos tämä on opiskelijan ensisijainen opiskeluoikeus tässä oppilaitoksessa, ilmoitetaan tässä ensisijaisuuden tiedot")
  tila: KorkeakoulunOpiskeluoikeudenTila,
  lisätiedot: Option[KorkeakoulunOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[KorkeakouluSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  @SyntheticProperty
  virtaVirheet: List[VirtaVirhe] = List.empty,
  synteettinen: Boolean = false,
  @KoodistoUri("virtaopiskeluoikeudenluokittelu")
  luokittelu: Option[List[Koodistokoodiviite]]
) extends Opiskeluoikeus with Equals {
  override def canEqual(that: Any): Boolean = that.isInstanceOf[KorkeakoulunOpiskeluoikeus]
  override def equals(that: Any): Boolean = that match {
    case that: KorkeakoulunOpiskeluoikeus if that.canEqual(this) =>
      (this.lähdejärjestelmänId.flatMap(_.id), that.lähdejärjestelmänId.flatMap(_.id)) match {
        case (Some(_), None) | (None, Some(_)) => false
        case (Some(thisOpiskeluoikeusAvain), Some(thatOpiskeluoikeusAvain)) => thisOpiskeluoikeusAvain == thatOpiskeluoikeusAvain
        case _ => this.suoritustenTunnisteet == that.suoritustenTunnisteet
      }
    case _ => false
  }

  override def hashCode: Int = this.lähdejärjestelmänId
    .flatMap(_.id.map(_.hashCode))
    .getOrElse(suoritustenTunnisteet.hashCode)

  override def versionumero = None
  override def sisältyyOpiskeluoikeuteen = None

  private def suoritustenTunnisteet =
    suoritukset.map(_.koulutusmoduuli.tunniste).sortBy(_.koodiarvo)
}

@Description("Korkeakoulun opiskeluoikeuden lisätiedot")
case class KorkeakoulunOpiskeluoikeudenLisätiedot(
  ensisijaisuus: Option[List[Aikajakso]] = None,
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite],
  @Title("Maksettavat lukuvuosimaksut")
  maksettavatLukuvuosimaksut: Option[Seq[KorkeakoulunOpiskeluoikeudenLukuvuosimaksu]] = None,
  lukukausiIlmoittautuminen: Option[Lukukausi_Ilmoittautuminen] = None,
  järjestäväOrganisaatio: Option[Oppilaitos] = None
) extends OpiskeluoikeudenLisätiedot {
  def ensisijaisuusVoimassa(d: LocalDate): Boolean = ensisijaisuus.exists(_.exists((j: Aikajakso) => j.contains(d)))
}

@Description("Korkeakoulun opiskeluoikeuden lukuvuosimaksut")
case class KorkeakoulunOpiskeluoikeudenLukuvuosimaksu(
  alku: LocalDate,
  loppu: Option[LocalDate],
  summa: Option[Int]
) extends Jakso

trait KorkeakouluSuoritus extends PäätasonSuoritus with MahdollisestiSuorituskielellinen with Toimipisteellinen {
  def toimipiste: Oppilaitos
}

case class KorkeakoulututkinnonSuoritus(
  @Title("Tutkinto")
  koulutusmoduuli: Korkeakoulututkinto,
  toimipiste: Oppilaitos,
  arviointi: Option[List[KorkeakoulunArviointi]],
  vahvistus: Option[Päivämäärävahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tutkintoon kuuluvien opintojaksojen suoritukset")
  @Title("Opintojaksot")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

case class KorkeakoulunOpintojaksonSuoritus(
  @Title("Opintojakso")
  koulutusmoduuli: KorkeakoulunOpintojakso,
  toimipiste: Oppilaitos,
  arviointi: Option[List[KorkeakoulunArviointi]],
  vahvistus: Option[Päivämäärävahvistus],
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("virtaopsuorluokittelu")
  luokittelu: Option[List[Koodistokoodiviite]],
  @Description("Opintojaksoon sisältyvien opintojaksojen suoritukset")
  @Title("Sisältyvät opintojaksot")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Muut kuin tutkintoon johtavat opiskeluoikeudet, joilla ei ole koulutuskoodia")
case class MuuKorkeakoulunSuoritus (
   @Title("Opiskeluoikeus")
   @FlattenInUI
   koulutusmoduuli: MuuKorkeakoulunOpinto,
   toimipiste: Oppilaitos,
   vahvistus: Option[Päivämäärävahvistus],
   suorituskieli: Option[Koodistokoodiviite],
   override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
   @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
   tyyppi: Koodistokoodiviite = Koodistokoodiviite("muukorkeakoulunsuoritus", koodistoUri = "suorituksentyyppi")
 ) extends KorkeakouluSuoritus with Arvioinniton {
}

@Description("Korkeakoulututkinnon tunnistetiedot")
case class Korkeakoulututkinto(
  tunniste: Koodistokoodiviite,
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  virtaNimi: Option[LocalizedString]
) extends Koulutus with Tutkinto with Laajuudeton {
  override def nimi: LocalizedString = virtaNimi.getOrElse(tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo)))
}

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KorkeakoulunOpintojakso(
  tunniste: PaikallinenKoodi,
  override val nimi: LocalizedString,
  laajuus: Option[Laajuus]
) extends KoulutusmoduuliValinnainenLaajuus with PaikallinenKoulutusmoduuli

@Description("Muun korkeakoulun opinnon tunnistetiedot")
case class MuuKorkeakoulunOpinto(
  @Title("Opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  tunniste: Koodistokoodiviite,
  nimi: LocalizedString,
  laajuus: Option[Laajuus]
) extends KoulutusmoduuliValinnainenLaajuus

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  nimi: Option[LocalizedString],
  @KoodistoUri("virtaopiskeluoikeudentila")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso {
  def opiskeluoikeusPäättynyt = List("3", "4", "5").contains(tila.koodiarvo)
}

trait KorkeakoulunArviointi extends ArviointiPäivämäärällä {
  def hyväksytty = true
}

case class KorkeakoulunKoodistostaLöytyväArviointi(
  @KoodistoUri("virtaarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends KoodistostaLöytyväArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class KorkeakoulunPaikallinenArviointi(
  @Description("Paikallinen arvosana, jota ei löydy kansallisesta koodistosta")
  arvosana: KorkeakoulunPaikallinenArvosana,
  päivä: LocalDate
) extends KorkeakoulunArviointi {
  def arvosanaKirjaimin = arvosana.nimi
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

@Description("Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa")
case class KorkeakoulunPaikallinenArvosana(
  @Description("Koodin yksilöivä tunniste käytetyssä koodistossa")
  @Title("Tunniste")
  @Discriminator
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  nimi: LocalizedString,
  @Description("Koodiston tunniste. Esimerkiksi Virta-järjestelmästä saatavissa arvioinneissa käytetään virta/x, missä x on arviointiasteikon tunniste. Jos koodistolla ei ole tunnistetta, voidaan kenttä jättää tyhjäksi")
  @Title("Koodisto-URI")
  koodistoUri: Option[String] = None
) extends PaikallinenKoodiviite

case class Lukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[Lukukausi_Ilmoittautumisjakso]
)

case class Lukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  tila: Koodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean] = None,
  @SensitiveData(Set(Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU))
  @Deprecated("Ei käytössä 1.1.2021 eteenpäin")
  ythsMaksettu: Option[Boolean] = None,
  @Title("Lukuvuosimaksu")
  maksetutLukuvuosimaksut: Option[Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu] = None
) extends Jakso

case class Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  @Title("Maksettu kokonaan")
  maksettu: Option[Boolean] = None,
  summa: Option[Int] = None,
  apuraha: Option[Int] = None
)

trait VirtaVirhe {
  val tyyppi: String
  val arvo: String
}

@OnlyWhen("tyyppi", "Duplikaatti")
case class Duplikaatti (
  tyyppi: String = "Duplikaatti",
  arvo: String
) extends VirtaVirhe

@OnlyWhen("tyyppi", "OpiskeluoikeusAvaintaEiLöydy")
case class OpiskeluoikeusAvaintaEiLöydy (
  tyyppi: String = "OpiskeluoikeusAvaintaEiLöydy",
  arvo: String
) extends VirtaVirhe
