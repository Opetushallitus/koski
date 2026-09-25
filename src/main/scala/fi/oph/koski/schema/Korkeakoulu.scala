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
  @VirtaDerived("Aina tyhjä: Virran opiskeluoikeuksia ei tallenneta Koskeen")
  oid: Option[String] = None,
  @VirtaSource("Opiskeluoikeus/@avain", "lähdejärjestelmä on aina virta")
  @VirtaNote("Jos sama avain on vastauksessa usealla opiskeluoikeudella (linkitetyt opiskelijat), id on avain.opiskelijaAvain, jotta se yksilöi. Synteettisellä opiskeluoikeudella id puuttuu. Tämä on korkeakoulun opiskeluoikeuden ainoa pysyvä tunniste, jolla mm. suoritusjaot löytävät sen uudelleen.")
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  @VirtaSource("Opiskeluoikeus/Organisaatio[Rooli=3]/Koodi | Organisaatio[Rooli=5]/Koodi | Myontaja", "ensimmäinen organisaatiopalvelusta löytyvä tässä järjestyksessä")
  @VirtaNote("Siirto-opiskelijalla (SiirtoOpiskelija-elementti) lähdeorganisaatio (rooli 3) ohitetaan. Oppilaitoksen nimi haetaan viimeisen tilajakson AlkuPvm:n mukaisena, jos tila on päättävä; muuten käytetään nykyistä nimeä, ja tilajaksottomalla opiskeluoikeudella suoritusten viimeisimmän vahvistuspäivän mukaista. Jos mikään koodi ei löydy organisaatiopalvelusta (esim. ulkomainen korkeakoulu), oppilaitos jää tyhjäksi eikä Koski luo opiskeluoikeudelle päätason suoritusta; opiskeluoikeus jätetään pois, jos sille ei jää yhtään suoritusta.")
  oppilaitos: Option[Oppilaitos],
  @VirtaDerived("Aina tyhjä")
  koulutustoimija: Option[Koulutustoimija] = None,
  @VirtaDerived("Aina tyhjä")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @VirtaSource("Opiskeluoikeus/LoppuPvm", "arvo 2112-12-21 tulkitaan puuttuvaksi")
  override val päättymispäivä: Option[LocalDate] = None,
  @VirtaSource("Opiskeluoikeus/Tila")
  tila: KorkeakoulunOpiskeluoikeudenTila,
  @VirtaSource("Opiskeluoikeus", "ks. lisätietojen kentät")
  lisätiedot: Option[KorkeakoulunOpiskeluoikeudenLisätiedot] = None,
  @VirtaSource("Opintosuoritus[Laji=1 tai 2]", "opiskeluoikeuteen kuuluvat juuritason suoritukset")
  @VirtaNote("Suoritus kuuluu opiskeluoikeuteen, jos sen @opiskeluoikeusAvain (ja @opiskelijaAvain) on tämän opiskeluoikeuden avain tai, jos suorituksella ei ole omaa @opiskeluoikeusAvain-arvoa, jokin siihen sisältyvä suoritus kuuluu. Tutkintoon johtavalle opiskeluoikeudelle, jolla on Jakso/Koulutuskoodi mutta ei valmista tutkintosuoritusta, Koski luo päätason tutkintosuorituksen; muille kuin tutkintoon johtaville sekä tutkintoon johtaville ilman koulutuskoodia luodaan MuuKorkeakoulunSuoritus. Opintojaksot ovat omia päätason suorituksiaan, ellei niitä sijoiteta tutkinnon alle tai (Opiskeluoikeus/Tyyppi 8 ja 13) MuuKorkeakoulunSuorituksen alle.")
  suoritukset: List[KorkeakouluSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  @VirtaDerived("Aina korkeakoulutus")
  tyyppi: Koodistokoodiviite,
  @SyntheticProperty
  @VirtaDerived("Konversiossa havaitut viittausvirheet Opintosuoritus/Sisaltyvyys-avaimissa; sama virhe voi toistua usealla saman vastauksen opiskeluoikeudella")
  virtaVirheet: List[VirtaVirhe] = List.empty,
  @VirtaDerived("true, kun opiskeluoikeus on koottu suorituksista, joilla ei ole opiskeluoikeutta Virrassa; ryhmitelty toimipisteen mukaan")
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
  @VirtaNote("Ilmoittautuminen kohdistuu opiskeluoikeuteen @opiskeluoikeusAvain-attribuutilla. Avaimeton ilmoittautuminen kohdistetaan, jos sen Myontaja on sama oppilaitos ja ilmoittautumisjakso osuu opiskeluoikeuden aktiiviseen (Tila/Koodi=1) tilajaksoon; tilajakso päättyy seuraavan alkaessa ja viimeinen on avoin.")
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
  @VirtaNote("Kohdistus kuten lukukausi-ilmoittautumisilla, mutta avaimettomassa tapauksessa Myontaja-koodia verrataan opiskeluoikeuden Virta-koodeihin (Myontaja, roolit 3 ja 5) eikä ratkaistuun oppilaitokseen, koska fuusiotapauksissa useampi koodi osoittaa samaan organisaatioon. Saman @avain-arvon toistuvat jaksot (fuusioduplikaatit) poistetaan.")
  liikkuvuusjaksot: Option[List[Liikkuvuusjakso]] = None,
  @Title("Opettajan pedagogiset opinnot")
  @InfoDescription("opettajan kelpoisuuden määritelmä")
  @KoodistoUri("virtapatevyys")
  @VirtaSource("Opintosuoritus/Patevyys", "opiskeluoikeuden juuritason suorituksilta, ei sisältyviltä; vain pedagogisten opintojen koodiarvot koodistosta virtapatevyys")
  opettajanPedagogisetOpinnot: Option[List[Koodistokoodiviite]],
  @Title("Opetettavan aineen opinnot")
  @InfoDescription("opetettavan aineen kelpoisuuden määritelmä")
  @KoodistoUri("virtapatevyys")
  @VirtaSource("Opintosuoritus/Patevyys", "opiskeluoikeuden juuritason suorituksilta, ei sisältyviltä; vain opetettavien aineiden koodiarvot koodistosta virtapatevyys")
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
  @VirtaSource("Opiskeluoikeus[@avain=liittyvä]/Myontaja", "liittyvän opiskeluoikeuden oppilaitos samalla hakujärjestyksellä kuin opiskeluoikeuden oppilaitos, nykyisellä nimellä, jos se on samassa vastauksessa")
  oppilaitos: Option[Oppilaitos] = None,
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  @VirtaSource("Opiskeluoikeus[@avain=liittyvä]/Tyyppi", "jos liittyvä opiskeluoikeus on samassa vastauksessa")
  tyyppi: Option[Koodistokoodiviite] = None
)

case class KoulutuskuntaJakso(
  @VirtaSource("Opiskeluoikeus/Jakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/Jakso/LoppuPvm")
  loppu: Option[LocalDate],
  @KoodistoUri("kunta")
  @VirtaSource("Opiskeluoikeus/Jakso/Koulutuskunta")
  koulutuskunta: Koodistokoodiviite
) extends Jakso

case class RahoituslähdeJakso(
  @VirtaSource("Opiskeluoikeus/Jakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Opiskeluoikeus/Jakso/LoppuPvm")
  loppu: Option[LocalDate],
  @KoodistoUri("virtarahoituslahde")
  @VirtaSource("Opiskeluoikeus/Jakso/Rahoituslahde")
  rahoituslähde: Koodistokoodiviite
) extends Jakso

@Description("Koulutusala Virran luokituksen mukaan. Virrassa koodiarvo yksilöidään versio-attribuutilla, joten kullekin luokitukselle on oma kenttänsä.")
case class KorkeakoulunKoulutusala(
  @Title("Opintoala 1995")
  @KoodistoUri("opintoalaoph1995")
  @VirtaSource("Koulutusala/Koodi[@versio=opm95opa]", "Koulutusala on sekä Opiskeluoikeus- että Opintosuoritus-elementillä; vanhassa muodossa koodi ja @versio ovat suoraan Koulutusala-elementillä")
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
  koodi: String,
  @Description("Oppilaitos, jos koodi on tunnistettava oppilaitosnumero")
  oppilaitos: Option[Oppilaitos] = None
)

case class Liikkuvuusjakso(
  @VirtaSource("Liikkuvuusjakso/AlkuPvm")
  alku: LocalDate,
  @VirtaSource("Liikkuvuusjakso/LoppuPvm")
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
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu/LoppuPvm")
  loppu: Option[LocalDate],
  @VirtaSource("Opiskeluoikeus/LukuvuosiMaksu/Summa")
  summa: Option[Int]
) extends Jakso

sealed trait KorkeakouluSuoritus extends PäätasonSuoritus with MahdollisestiSuorituskielellinen with Toimipisteellinen {
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
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  hyväksilukupäivä: Option[LocalDate] = None,
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Hidden
  @SkipSerialization
  lisätieto: Option[LocalizedString] = None,
  @Description("Tutkinnon tai opintojen vaadittu laajuus")
  vaadittuLaajuus: Option[Laajuus] = None,
  liittyvätOpiskeluoikeudet: Option[List[LiittyväOpiskeluoikeus]] = None,
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
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  hyväksilukupäivä: Option[LocalDate] = None,
  @Description("Tieto siitä, onko opintosuoritus opinnäytetyö")
  opinnäytetyö: Option[Boolean] = None,
  @Description("Virran opintosuorituksen laji. Arvolla 3 (ei huomioitava) merkitty suoritus on väliaikainen kirjaus, jonka laajuus sisältyy jo ylemmän tason suoritukseen.")
  @KoodistoUri("virtaopintosuorituksenlaji")
  @Hidden
  laji: Option[Koodistokoodiviite] = None,
  @Hidden
  lähdeorganisaatio: Option[KorkeakoulunLähdeorganisaatio] = None,
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Hidden
  @SkipSerialization
  lisätieto: Option[LocalizedString] = None,
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
   @Description("Tutkinnon tai opintojen vaadittu laajuus")
   vaadittuLaajuus: Option[Laajuus] = None,
   @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
   tyyppi: Koodistokoodiviite = Koodistokoodiviite("muukorkeakoulunsuoritus", koodistoUri = "suorituksentyyppi")
 ) extends KorkeakouluSuoritus with Arvioinniton {
}

@Description("Korkeakoulututkinnon tunnistetiedot")
case class Korkeakoulututkinto(
  tunniste: Koodistokoodiviite,
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  virtaNimi: Option[LocalizedString],
  koulutusala: Option[KorkeakoulunKoulutusala] = None
) extends Koulutus with Tutkinto with Laajuudeton {
  override def nimi: LocalizedString = virtaNimi.getOrElse(tunniste.nimi.getOrElse(unlocalized(tunniste.koodiarvo)))
}

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KorkeakoulunOpintojakso(
  tunniste: PaikallinenKoodi,
  nimi: LocalizedString,
  laajuus: Option[Laajuus],
  koulutusala: Option[KorkeakoulunKoulutusala] = None
) extends KoulutusmoduuliValinnainenLaajuus

@Description("Muun korkeakoulun opinnon tunnistetiedot")
case class MuuKorkeakoulunOpinto(
  @Title("Opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  tunniste: Koodistokoodiviite,
  nimi: LocalizedString,
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
  @Description("Päivämäärä, jolloin ilmoittautuminen on tehty")
  ilmoittautumispäivä: Option[LocalDate] = None,
  ylioppilaskunnanJäsen: Option[Boolean] = None,
  @SensitiveData(Set(Rooli.MIGRI, Rooli.HSL, Rooli.SUOMIFI))
  @Deprecated("ei kaytossa yths maksettu")
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
