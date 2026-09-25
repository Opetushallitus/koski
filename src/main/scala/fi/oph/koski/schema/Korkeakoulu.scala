package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.unlocalized

import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, Discriminator, OnlyWhen, SkipSerialization, SyntheticProperty, Title}
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.annotation.Deprecated

case class KorkeakoulunOpiskeluoikeus(
  @VirtaDerived("Aina tyhjä")
  oid: Option[String] = None,
  @VirtaSource("Opiskeluoikeus/@avain", "lähdejärjestelmä on aina virta")
  @VirtaNote("Jos sama avain on vastauksessa usealla opiskeluoikeudella, id on avain.opiskelijaAvain. Synteettisellä opiskeluoikeudella id puuttuu.")
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  @VirtaSource("Opiskeluoikeus/Organisaatio[Rooli=3]/Koodi | Organisaatio[Rooli=5]/Koodi | Myontaja", "ensimmäinen organisaatiopalvelusta löytyvä tässä järjestyksessä")
  @VirtaNote("Siirto-opiskelijalla rooli 3 ohitetaan; jos mikään koodi ei löydy organisaatiopalvelusta, oppilaitos jää tyhjäksi. Nimi haetaan viimeisen tilajakson AlkuPvm:n mukaisena, jos tila on päättävä, muuten nykyinen nimi.")
  oppilaitos: Option[Oppilaitos],
  @VirtaDerived("Aina tyhjä")
  koulutustoimija: Option[Koulutustoimija] = None,
  @VirtaDerived("Aina tyhjä")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @VirtaSource("Opiskeluoikeus/LoppuPvm", "2112-12-21 = puuttuva")
  override val päättymispäivä: Option[LocalDate] = None,
  @VirtaSource("Opiskeluoikeus/Tila")
  tila: KorkeakoulunOpiskeluoikeudenTila,
  @VirtaSource("Opiskeluoikeus", "ks. lisätietojen kentät")
  lisätiedot: Option[KorkeakoulunOpiskeluoikeudenLisätiedot] = None,
  @VirtaSource("Opintosuoritus[Laji=1 tai 2]", "opiskeluoikeuteen kuuluvat juuritason suoritukset")
  @VirtaNote("Suoritus kuuluu opiskeluoikeuteen, jos sen @opiskeluoikeusAvain (ja @opiskelijaAvain) on tämän opiskeluoikeuden avain tai, avaimen puuttuessa, jokin siihen sisältyvä suoritus kuuluu. Koski luo tarvittaessa päätason tutkintosuorituksen tai MuuKorkeakoulunSuorituksen, ks. niiden kentät.")
  suoritukset: List[KorkeakouluSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  @VirtaDerived("Aina korkeakoulutus")
  tyyppi: Koodistokoodiviite,
  @SyntheticProperty
  @VirtaDerived("Sisaltyvyys-viittausten virheet")
  @VirtaNote("Virhe voi toistua usealla saman vastauksen opiskeluoikeudella.")
  virtaVirheet: List[VirtaVirhe] = List.empty,
  @VirtaDerived("true, jos opiskeluoikeutta ei ole Virrassa")
  @VirtaNote("Opiskeluoikeus on koottu toimipisteittäin suorituksista, joilla ei ole opiskeluoikeutta Virrassa.")
  synteettinen: Boolean = false,
  @KoodistoUri("virtaopiskeluoikeudenluokittelu")
  @VirtaSource("Opiskeluoikeus/Jakso/Luokittelu", "vain koodistosta virtaopiskeluoikeudenluokittelu löytyvät arvot")
  luokittelu: Option[List[Koodistokoodiviite]],
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
  @Description("Jos tämä on opiskelijan ensisijainen opiskeluoikeus tässä oppilaitoksessa, ilmoitetaan tässä ensisijaisuuden tiedot")
  @VirtaSource("Opiskeluoikeus/Ensisijaisuus", "AlkuPvm ja LoppuPvm")
  ensisijaisuus: Option[List[Aikajakso]] = None,
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  @VirtaSource("Opiskeluoikeus/Tyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite],
  @Title("Maksettavat lukuvuosimaksut")
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu")
  maksettavatLukuvuosimaksut: Option[Seq[KorkeakoulunOpiskeluoikeudenLukuvuosimaksu]] = None,
  @VirtaSource("LukukausiIlmoittautuminen", "tähän opiskeluoikeuteen kohdistuvat ilmoittautumiset")
  @VirtaNote("Kohdistus @opiskeluoikeusAvain-attribuutilla. Avaimeton ilmoittautuminen kohdistetaan, jos sen Myontaja on sama oppilaitos ja jakso osuu aktiiviseen (Tila/Koodi=1) tilajaksoon.")
  lukukausiIlmoittautuminen: Option[Lukukausi_Ilmoittautuminen] = None,
  @VirtaSource("Opiskeluoikeus/Organisaatio[Rooli=2]/Koodi", "vain kun eri kuin Myontaja")
  järjestäväOrganisaatio: Option[Oppilaitos] = None,
  @Title("Koulutuskunnat")
  @VirtaSource("Opiskeluoikeus/Jakso", "AlkuPvm, LoppuPvm ja Koulutuskunta")
  koulutuskuntaJaksot: List[KoulutuskuntaJakso] = Nil,
  @Title("Rahoituslähteet")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @VirtaSource("Opiskeluoikeus/Jakso", "AlkuPvm, LoppuPvm ja Rahoituslahde; vain koodistosta virtarahoituslahde löytyvät arvot")
  rahoituslähdeJaksot: Option[List[RahoituslähdeJakso]] = None,
  @Title("Liikkuvuusjaksot")
  @VirtaSource("Liikkuvuusjakso", "tähän opiskeluoikeuteen kohdistuvat jaksot")
  @VirtaNote("Kohdistus kuten lukukausi-ilmoittautumisilla, mutta avaimettomassa tapauksessa Myontaja-koodia verrataan opiskeluoikeuden Virta-koodeihin (Myontaja, roolit 3 ja 5). Saman @avain-arvon toistuvat jaksot poistetaan.")
  liikkuvuusjaksot: Option[List[Liikkuvuusjakso]] = None,
  @Title("Opettajan pedagogiset opinnot")
  @InfoDescription("opettajan kelpoisuuden määritelmä")
  @KoodistoUri("virtapatevyys")
  @VirtaSource("Opintosuoritus/Patevyys", "juuritason suorituksilta; vain pedagogisten opintojen koodiarvot")
  opettajanPedagogisetOpinnot: Option[List[Koodistokoodiviite]],
  @Title("Opetettavan aineen opinnot")
  @InfoDescription("opetettavan aineen kelpoisuuden määritelmä")
  @KoodistoUri("virtapatevyys")
  @VirtaSource("Opintosuoritus/Patevyys", "juuritason suorituksilta; vain opetettavien aineiden koodiarvot")
  opetettavanAineenOpinnot: Option[List[Koodistokoodiviite]],
  @Description("Siirto-opiskelijan siirtopäivä ja lähdeoppilaitos")
  @VirtaSource("Opiskeluoikeus/SiirtoOpiskelija")
  siirtoOpiskelija: Option[SiirtoOpiskelija] = None,
  @VirtaSource("Opiskeluoikeus/Koulutusala")
  koulutusala: Option[KorkeakoulunKoulutusala] = None,
) extends OpiskeluoikeudenLisätiedot {
  def ensisijaisuusVoimassa(d: LocalDate): Boolean = ensisijaisuus.exists(_.exists((j: Aikajakso) => j.contains(d)))
}

case class SiirtoOpiskelija(
  @VirtaSource("Opiskeluoikeus/SiirtoOpiskelija/SiirtoPvm")
  siirtoPäivä: LocalDate,
  @VirtaSource("Opiskeluoikeus/Organisaatio[Rooli=3]/Koodi", "haetaan organisaatiopalvelusta")
  lähdeOrganisaatio: Option[Oppilaitos]
)

@Description("Opiskeluoikeus, johon tämä opiskeluoikeus antaa mahdollisuuden jatkaa")
case class LiittyväOpiskeluoikeus(
  @Description("Liittyvän opiskeluoikeuden lähdejärjestelmän id, sama tunniste jolla se esiintyy tässä vastauksessa. Korkeakoulun opiskeluoikeudella ei ole oidia, joten tämä on sen yksilöivä tunniste.")
  @VirtaSource("Opiskeluoikeus/Liittyvyys/@liittyvaOpiskeluoikeusAvain", "samalla duplikaattisäännöllä kuin opiskeluoikeuden lähdejärjestelmänId")
  lähdejärjestelmänId: String,
  @Description("Liittyvän opiskeluoikeuden oppilaitos, jos se on mukana samassa vastauksessa")
  @VirtaSource("Opiskeluoikeus[@avain=liittyvä]/Myontaja", "jos liittyvä opiskeluoikeus on samassa vastauksessa")
  @VirtaNote("Haetaan samalla järjestyksellä kuin opiskeluoikeuden oppilaitos, nykyisellä nimellä.")
  oppilaitos: Option[Oppilaitos] = None,
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  @VirtaSource("Opiskeluoikeus[@avain=liittyvä]/Tyyppi", "jos liittyvä opiskeluoikeus on samassa vastauksessa")
  tyyppi: Option[Koodistokoodiviite] = None
)

case class KoulutuskuntaJakso(
  @VirtaSource("Opiskeluoikeus/Jakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/Jakso/LoppuPvm", "2112-12-21 = puuttuva")
  loppu: Option[LocalDate],
  @KoodistoUri("kunta")
  @VirtaSource("Opiskeluoikeus/Jakso/Koulutuskunta")
  koulutuskunta: Koodistokoodiviite
) extends Jakso

case class RahoituslähdeJakso(
  @VirtaSource("Opiskeluoikeus/Jakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/Jakso/LoppuPvm", "2112-12-21 = puuttuva")
  loppu: Option[LocalDate],
  @KoodistoUri("virtarahoituslahde")
  @VirtaSource("Opiskeluoikeus/Jakso/Rahoituslahde")
  rahoituslähde: Koodistokoodiviite
) extends Jakso

@Description("Koulutusala Virran luokituksen mukaan. Virrassa koodiarvo yksilöidään versio-attribuutilla, joten kullekin luokitukselle on oma kenttänsä.")
case class KorkeakoulunKoulutusala(
  @Title("Opintoala 1995")
  @KoodistoUri("opintoalaoph1995")
  @VirtaSource("Koulutusala/Koodi[@versio=opm95opa]", "vanhassa muodossa koodi ja @versio suoraan Koulutusala-elementillä")
  @VirtaNote("Koulutusala on sekä Opiskeluoikeus- että Opintosuoritus-elementillä.")
  opintoala1995: Option[Koodistokoodiviite] = None,
  @Title("OKM:n ohjauksen ala")
  @KoodistoUri("okmohjauksenala")
  @VirtaSource("Koulutusala/Koodi[@versio=ohjausala]")
  okmOhjausala: Option[Koodistokoodiviite] = None,
  @Title("Koulutusala 2002")
  @KoodistoUri("koulutusalaoph2002")
  @VirtaSource("Koulutusala/Koodi[@versio=opmala]")
  koulutusala2002: Option[Koodistokoodiviite] = None,
  @Description("Koulutusalan osuus suorituksesta")
  @VirtaSource("Koulutusala/Osuus")
  osuus: Option[Double] = None
)

@Description("Organisaatio, josta opintosuoritus on hyväksiluettu")
case class KorkeakoulunLähdeorganisaatio(
  @Description("Virran organisaatiokoodi. Yleensä oppilaitosnumero, mutta myös UK (ulkomainen korkeakoulu), UM (ulkomainen muu oppilaitos) tai 99 (muu oppilaitos).")
  @VirtaSource("Opintosuoritus/Organisaatio[Rooli=3]/Koodi")
  koodi: String,
  @Description("Oppilaitos, jos koodi on tunnistettava oppilaitosnumero")
  @VirtaDerived("Organisaatiopalvelusta, jos koodi on viisinumeroinen")
  oppilaitos: Option[Oppilaitos] = None
)

case class Liikkuvuusjakso(
  @VirtaSource("Liikkuvuusjakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Liikkuvuusjakso/LoppuPvm", "2112-12-21 = puuttuva")
  loppu: Option[LocalDate],
  @KoodistoUri("virtaliikkuvuudensuunta")
  @VirtaSource("Liikkuvuusjakso/Suunta", "jakso jätetään pois, jos Suunta, Maa, Tyyppi tai Liikkuvuusohjelma ei löydy koodistostaan")
  suunta: Koodistokoodiviite,
  @KoodistoUri("maatjavaltiot2")
  @VirtaSource("Liikkuvuusjakso/Maa")
  maa: Koodistokoodiviite,
  @KoodistoUri("virtaliikkuvuudentyyppi")
  @VirtaSource("Liikkuvuusjakso/Tyyppi")
  tyyppi: Koodistokoodiviite,
  @KoodistoUri("virtaliikkuvuusohjelma")
  @VirtaSource("Liikkuvuusjakso/Liikkuvuusohjelma")
  liikkuvuusohjelma: Koodistokoodiviite,
  @KoodistoUri("liikkuvuudenluokittelu")
  @VirtaSource("Liikkuvuusjakso/Luokittelu", "vain koodistosta liikkuvuudenluokittelu löytyvät yksikirjaimiset arvot")
  luokittelu: Option[List[Koodistokoodiviite]] = None
) extends Jakso

@Description("Korkeakoulun opiskeluoikeuden lukuvuosimaksut")
case class KorkeakoulunOpiskeluoikeudenLukuvuosimaksu(
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu/LoppuPvm", "2112-12-21 = puuttuva")
  loppu: Option[LocalDate],
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu/Summa")
  summa: Option[Int]
) extends Jakso

sealed trait KorkeakouluSuoritus extends PäätasonSuoritus with MahdollisestiSuorituskielellinen with Toimipisteellinen {
  def toimipiste: Oppilaitos
}

case class KorkeakoulututkinnonSuoritus(
  @Title("Tutkinto")
  @VirtaSource("Opintosuoritus[Laji=1]/Koulutuskoodi", "Koskessa luodulla suorituksella viimeisin Opiskeluoikeus/Jakso/Koulutuskoodi")
  @VirtaNote("Koski luo tutkintosuorituksen, jos Jakso/Koulutuskoodia vastaavaa suoritusta ei ole Virrassa.")
  koulutusmoduuli: Korkeakoulututkinto,
  @VirtaSource("Opintosuoritus/Organisaatio[Rooli=3]/Koodi | Organisaatio[Rooli=5]/Koodi | Myontaja", "kuten opiskeluoikeuden oppilaitos; hyväksiluetulla suorituksella rooli 3 ohitetaan")
  @VirtaNote("Koskessa luodulla suorituksella opiskeluoikeuden oppilaitos.")
  toimipiste: Oppilaitos,
  @VirtaSource("Opintosuoritus/Arvosana", "ks. arvioinnin kentät; tyhjä, jos arvosanaa ei tunnisteta")
  arviointi: Option[List[KorkeakoulunArviointi]],
  @VirtaDerived("Arvioinnin päivä ja toimipiste")
  @VirtaNote("Koskessa luodulla suorituksella viimeisen tilajakson AlkuPvm, jos sen Koodi on 3, muuten tyhjä.")
  vahvistus: Option[Päivämäärävahvistus],
  @VirtaSource("Opintosuoritus/Kieli", "vain koodistosta kieli löytyvät arvot")
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tutkintoon kuuluvien opintojaksojen suoritukset")
  @Title("Opintojaksot")
  @VirtaSource("Opintosuoritus/Sisaltyvyys/@sisaltyvaOpintosuoritusAvain", "sisältyvät suoritukset lajista riippumatta")
  @VirtaNote("Jos opiskeluoikeudella on vain yksi tutkintosuoritus eikä sillä ole sisältyvyyksiä, irralliset opintojaksot siirretään sen alle. Koskessa luodulle suoritukselle opintojaksot ovat osasuorituksia vain, jos opiskeluoikeus ei ole päättynyt.")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  @VirtaSource("Opintosuoritus/HyvaksilukuPvm")
  hyväksilukupäivä: Option[LocalDate] = None,
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Hidden
  @SkipSerialization
  @VirtaSource("Opintosuoritus/JulkinenLisatieto", "@kieli-attribuutin mukaan kielistettynä")
  lisätieto: Option[LocalizedString] = None,
  @Description("Tutkinnon tai opintojen vaadittu laajuus")
  @VirtaSource("Opiskeluoikeus/Laajuus", "Opintopiste, muuten Opintoviikko; sama kaikilla opiskeluoikeuden päätason suorituksilla")
  vaadittuLaajuus: Option[Laajuus] = None,
  @VirtaSource("Opiskeluoikeus/Liittyvyys", "sama kaikilla opiskeluoikeuden päätason suorituksilla")
  liittyvätOpiskeluoikeudet: Option[List[LiittyväOpiskeluoikeus]] = None,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  @VirtaDerived("Aina korkeakoulututkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

case class KorkeakoulunOpintojaksonSuoritus(
  @Title("Opintojakso")
  @VirtaSource("Opintosuoritus", "juuritasolla vain Laji=2; sisältyvät suoritukset lajista riippumatta, ks. laji")
  koulutusmoduuli: KorkeakoulunOpintojakso,
  @VirtaSource("Opintosuoritus/Organisaatio[Rooli=3]/Koodi | Organisaatio[Rooli=5]/Koodi | Myontaja", "kuten opiskeluoikeuden oppilaitos; hyväksiluetulla suorituksella rooli 3 ohitetaan")
  toimipiste: Oppilaitos,
  @VirtaSource("Opintosuoritus/Arvosana", "ks. arvioinnin kentät; tyhjä, jos arvosanaa ei tunnisteta")
  arviointi: Option[List[KorkeakoulunArviointi]],
  @VirtaDerived("Arvioinnin päivä ja toimipiste")
  vahvistus: Option[Päivämäärävahvistus],
  @VirtaSource("Opintosuoritus/Kieli", "vain koodistosta kieli löytyvät arvot")
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoUri("virtaopsuorluokittelu")
  @VirtaSource("Opintosuoritus/Luokittelu", "vain koodistosta virtaopsuorluokittelu löytyvät arvot")
  luokittelu: Option[List[Koodistokoodiviite]],
  @Description("Opintojaksoon sisältyvien opintojaksojen suoritukset")
  @Title("Sisältyvät opintojaksot")
  @VirtaSource("Opintosuoritus/Sisaltyvyys/@sisaltyvaOpintosuoritusAvain", "sisältyvät suoritukset lajista riippumatta")
  override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None,
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  @VirtaSource("Opintosuoritus/HyvaksilukuPvm")
  hyväksilukupäivä: Option[LocalDate] = None,
  @Description("Tieto siitä, onko opintosuoritus opinnäytetyö")
  @VirtaSource("Opintosuoritus/Opinnaytetyo", "1 tai true = tosi")
  opinnäytetyö: Option[Boolean] = None,
  @Description("Virran opintosuorituksen laji. Arvolla 3 (ei huomioitava) merkitty suoritus on väliaikainen kirjaus, jonka laajuus sisältyy jo ylemmän tason suoritukseen.")
  @KoodistoUri("virtaopintosuorituksenlaji")
  @Hidden
  @VirtaSource("Opintosuoritus/Laji")
  laji: Option[Koodistokoodiviite] = None,
  @Hidden
  @VirtaSource("Opintosuoritus/Organisaatio[Rooli=3]")
  lähdeorganisaatio: Option[KorkeakoulunLähdeorganisaatio] = None,
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Hidden
  @SkipSerialization
  @VirtaSource("Opintosuoritus/JulkinenLisatieto", "@kieli-attribuutin mukaan kielistettynä")
  lisätieto: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  @VirtaDerived("Aina korkeakoulunopintojakso")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluSuoritus {
  override def tarvitseeVahvistuksen = false
}

@Description("Muut kuin tutkintoon johtavat opiskeluoikeudet, joilla ei ole koulutuskoodia")
case class MuuKorkeakoulunSuoritus (
   @Title("Opiskeluoikeus")
   @FlattenInUI
   @VirtaSource("Opiskeluoikeus", "ks. kentät")
   @VirtaNote("Luodaan opiskeluoikeudelle, jonka Tyyppi ei johda tutkintoon (muu kuin 1, 2, 3, 4, 6, 7) tai jonka millään Jakso-elementillä ei ole Koulutuskoodia, vaikka Virrassa olisi tutkintosuoritus. Edellyttää, että oppilaitos löytyy organisaatiopalvelusta.")
   koulutusmoduuli: MuuKorkeakoulunOpinto,
   @VirtaSource("Opiskeluoikeus/Organisaatio[Rooli=3]/Koodi | Organisaatio[Rooli=5]/Koodi | Myontaja", "opiskeluoikeuden oppilaitos")
   toimipiste: Oppilaitos,
   @VirtaDerived("Viimeisen tilajakson AlkuPvm, jos Koodi 3")
   @VirtaNote("Muuten tyhjä. Vahvistaja on toimipiste.")
   vahvistus: Option[Päivämäärävahvistus],
   @VirtaDerived("Aina tyhjä")
   suorituskieli: Option[Koodistokoodiviite],
   @VirtaSource("Opintosuoritus[Laji=2]", "opiskeluoikeuden opintojaksot, kun Opiskeluoikeus/Tyyppi on 8 tai 13")
   @VirtaNote("Muilla tyypeillä opintojaksot ovat omia päätason suorituksiaan.")
   override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]],
   @Description("Tutkinnon tai opintojen vaadittu laajuus")
   @VirtaSource("Opiskeluoikeus/Laajuus", "Opintopiste, muuten Opintoviikko")
   vaadittuLaajuus: Option[Laajuus] = None,
   @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
   @VirtaDerived("Aina muukorkeakoulunsuoritus")
   tyyppi: Koodistokoodiviite = Koodistokoodiviite("muukorkeakoulunsuoritus", koodistoUri = "suorituksentyyppi")
 ) extends KorkeakouluSuoritus with Arvioinniton {
}

@Description("Korkeakoulututkinnon tunnistetiedot")
case class Korkeakoulututkinto(
  @VirtaSource("Opintosuoritus/Koulutuskoodi", "Koskessa luodulla suorituksella viimeisin Opiskeluoikeus/Jakso/Koulutuskoodi")
  tunniste: Koodistokoodiviite,
  @VirtaDerived("Ei täytetä konversiossa")
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  @VirtaSource("Opiskeluoikeus/Jakso/Nimi", "viimeisimmän nimellisen jakson nimi, @kieli-attribuutin mukaan kielistettynä")
  virtaNimi: Option[LocalizedString],
  @VirtaSource("Opintosuoritus/Koulutusala", "Koskessa luodulla suorituksella tyhjä")
  koulutusala: Option[KorkeakoulunKoulutusala] = None
) extends Koulutus with Tutkinto with Laajuudeton {
  override def nimi: LocalizedString = virtaNimi.getOrElse(tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo)))
}

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KorkeakoulunOpintojakso(
  @VirtaSource("Opintosuoritus/@koulutusmoduulitunniste", "paikallinen koodi, nimenä suorituksen nimi")
  tunniste: PaikallinenKoodi,
  @VirtaSource("Opintosuoritus/Nimi", "@kieli-attribuutin mukaan kielistettynä; puuttuessa 'Suoritus: <avain>'")
  nimi: LocalizedString,
  @VirtaSource("Opintosuoritus/Laajuus", "Opintopiste, muuten Opintoviikko; puuttuessa sisältyvien laajuuksien summa opintopisteinä")
  laajuus: Option[Laajuus],
  @VirtaSource("Opintosuoritus/Koulutusala")
  koulutusala: Option[KorkeakoulunKoulutusala] = None
) extends KoulutusmoduuliValinnainenLaajuus

@Description("Muun korkeakoulun opinnon tunnistetiedot")
case class MuuKorkeakoulunOpinto(
  @Title("Opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  @VirtaSource("Opiskeluoikeus/Tyyppi")
  tunniste: Koodistokoodiviite,
  @VirtaSource("Opiskeluoikeus/Jakso/Nimi", "viimeisimmän nimellisen jakson nimi; puuttuessa Opiskeluoikeus/@koulutusmoduulitunniste tai tyypin nimi koodistosta")
  nimi: LocalizedString,
  @VirtaSource("Opiskeluoikeus/Laajuus", "Opintopiste, muuten Opintoviikko")
  laajuus: Option[Laajuus]
) extends KoulutusmoduuliValinnainenLaajuus

case class KorkeakoulunOpiskeluoikeudenTila(
  @VirtaSource("Opiskeluoikeus/Tila", "AlkuPvm-järjestyksessä")
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  @VirtaSource("Opiskeluoikeus/Tila/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/Jakso/Nimi", "viimeisimmän nimellisen jakson nimi, sama kaikilla tilajaksoilla")
  nimi: Option[LocalizedString],
  @KoodistoUri("virtaopiskeluoikeudentila")
  @VirtaSource("Opiskeluoikeus/Tila/Koodi")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso {
  def opiskeluoikeusPäättynyt: Boolean =
    OpiskeluoikeudenPäättymistila.korkeakoulu(tila.koodiarvo)
}

trait KorkeakoulunArviointi extends ArviointiPäivämäärällä {
  def hyväksytty = true
}

case class KorkeakoulunKoodistostaLöytyväArviointi(
  @KoodistoUri("virtaarvosana")
  @VirtaSource("Opintosuoritus/Arvosana/*", "ensimmäisen alielementin (esim. Viisiportainen) arvo; vain koodistosta virtaarvosana löytyvät")
  arvosana: Koodistokoodiviite,
  @VirtaSource("Opintosuoritus/SuoritusPvm")
  päivä: LocalDate
) extends KoodistostaLöytyväArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class KorkeakoulunPaikallinenArviointi(
  @Description("Paikallinen arvosana, jota ei löydy kansallisesta koodistosta")
  @VirtaSource("Opintosuoritus/Arvosana/Muu", "kun arvosana on oppilaitoksen omalla asteikolla")
  @VirtaNote("Tyhjä, jos Muu/Koodi ei vastaa mitään AsteikkoArvosana/@avain-arvoa.")
  arvosana: KorkeakoulunPaikallinenArvosana,
  @VirtaSource("Opintosuoritus/SuoritusPvm")
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
  @VirtaSource("Opintosuoritus/Arvosana/Muu/Asteikko/AsteikkoArvosana[@avain=Muu/Koodi]/Koodi", "asteikon arvosana, jonka @avain on Muu/Koodi")
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  @VirtaSource("Opintosuoritus/Arvosana/Muu/Asteikko/AsteikkoArvosana[@avain=Muu/Koodi]/Nimi", "aina suomenkielisenä; puuttuessa AsteikkoArvosana/Koodi")
  nimi: LocalizedString,
  @Description("Koodiston tunniste. Esimerkiksi Virta-järjestelmästä saatavissa arvioinneissa käytetään virta/x, missä x on arviointiasteikon tunniste. Jos koodistolla ei ole tunnistetta, voidaan kenttä jättää tyhjäksi")
  @Title("Koodisto-URI")
  @VirtaSource("Opintosuoritus/Arvosana/Muu/Asteikko/@avain", "muodossa virta/<avain>")
  koodistoUri: Option[String] = None
) extends PaikallinenKoodiviite

case class Lukukausi_Ilmoittautuminen(
  @VirtaSource("LukukausiIlmoittautuminen", "AlkuPvm-järjestyksessä")
  ilmoittautumisjaksot: List[Lukukausi_Ilmoittautumisjakso]
)

case class Lukukausi_Ilmoittautumisjakso(
  @VirtaSource("LukukausiIlmoittautuminen/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("LukukausiIlmoittautuminen/LoppuPvm", "2112-12-21 = puuttuva")
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  @VirtaSource("LukukausiIlmoittautuminen/Tila", "koodistosta puuttuva arvo tulkitaan koodiksi 4")
  tila: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin ilmoittautuminen on tehty")
  @VirtaSource("LukukausiIlmoittautuminen/IlmoittautumisPvm")
  ilmoittautumispäivä: Option[LocalDate] = None,
  @VirtaSource("LukukausiIlmoittautuminen/YlioppilaskuntaJasen", "1 tai true = tosi")
  ylioppilaskunnanJäsen: Option[Boolean] = None,
  @SensitiveData(Set(Rooli.MIGRI, Rooli.HSL, Rooli.SUOMIFI))
  @Deprecated("ei kaytossa yths maksettu")
  @VirtaSource("LukukausiIlmoittautuminen/YTHSMaksu", "1 tai true = tosi")
  ythsMaksettu: Option[Boolean] = None,
  @Title("Lukuvuosimaksu")
  @VirtaSource("LukukausiIlmoittautuminen/LukuvuosiMaksu")
  maksetutLukuvuosimaksut: Option[Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu] = None
) extends Jakso

case class Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  @Title("Maksettu kokonaan")
  @VirtaSource("LukukausiIlmoittautuminen/LukuvuosiMaksu/Maksettu", "1 tai true = tosi")
  maksettu: Option[Boolean] = None,
  @VirtaSource("LukukausiIlmoittautuminen/LukuvuosiMaksu/Summa")
  summa: Option[Int] = None,
  @VirtaSource("LukukausiIlmoittautuminen/LukuvuosiMaksu/Apuraha")
  apuraha: Option[Int] = None
)

trait VirtaVirhe {
  val tyyppi: String
  val arvo: String
}

@OnlyWhen("tyyppi", "Duplikaatti")
case class Duplikaatti (
  @VirtaDerived("Aina Duplikaatti")
  tyyppi: String = "Duplikaatti",
  @VirtaSource("Opintosuoritus/Sisaltyvyys/@sisaltyvaOpintosuoritusAvain", "avain, joka esiintyy vastauksessa usealla suorituksella")
  arvo: String
) extends VirtaVirhe

@OnlyWhen("tyyppi", "OpiskeluoikeusAvaintaEiLöydy")
case class OpiskeluoikeusAvaintaEiLöydy (
  @VirtaDerived("Aina OpiskeluoikeusAvaintaEiLöydy")
  tyyppi: String = "OpiskeluoikeusAvaintaEiLöydy",
  @VirtaSource("Opintosuoritus/Sisaltyvyys/@sisaltyvaOpintosuoritusAvain", "avain, jota ei löydy vastauksesta")
  arvo: String
) extends VirtaVirhe
