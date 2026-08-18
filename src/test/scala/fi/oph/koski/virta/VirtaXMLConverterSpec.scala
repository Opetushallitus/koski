package fi.oph.koski.virta

import fi.oph.koski.TestEnvironment
import fi.oph.koski.documentation.ExampleData.{laajuusOpintopisteissä, laajuusOpintoviikoissa}
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppilaitos.MockOppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.{Files, XML}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.xml.Elem

class VirtaXMLConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers with OptionValues {

  private val converter = VirtaXMLConverter(new MockOppilaitosRepository, MockKoodistoViitePalvelu, MockOrganisaatioRepository)

  private def convertSuoritus(suoritus: Elem) = converter.convertSuoritus(None, suoritus, List(suoritus))

  // Tällä päivämäärällä palautuu organisaation nimi " -vanha" loppuliitteellä MockOrganisaatioRepositorystä
  private val organisaatioVanhallaNimelläPvm = LocalDate.of(2010, 10, 10)

  def baseSuoritus: Elem = suoritusWithOrganisaatio(None)

  def tutkintoSuoritus(kieli: Option[String] = Some("fi")): Elem =
    <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="tutkinto-123" avain="tutkinto-avain-1">
      <virta:SuoritusPvm>2014-05-30</virta:SuoritusPvm>
      <virta:Laajuus>
        <virta:Opintopiste>180.0</virta:Opintopiste>
      </virta:Laajuus>
      <virta:Arvosana>
        <virta:Hyvaksytty>HYV</virta:Hyvaksytty>
      </virta:Arvosana>
      <virta:Myontaja>10076</virta:Myontaja>
      <virta:Laji>1</virta:Laji>
      <virta:Nimi kieli="fi">Kauppatieteiden kandidaatti</virta:Nimi>
      {kieli.map(k => <virta:Kieli>{k}</virta:Kieli>).getOrElse(scala.xml.NodeSeq.Empty)}
      <virta:Koulutuskoodi>612103</virta:Koulutuskoodi>
    </virta:Opintosuoritus>

  def suoritusWithOrganisaatio(
    organisaatio: Option[Elem],
    suoritusPvm: String = "2014-05-30",
    luokittelu: Option[Int] = None,
    ilmanAinePätevyyksiä: Boolean = false,
    ilmanOpePätevyyksiä: Boolean = false
  ): Elem = <virta:Opintosuoritus valtakunnallinenKoulutusmoduulitunniste="" opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="Kul-49.3400" avain="1114935190">
    <virta:SuoritusPvm>{suoritusPvm}</virta:SuoritusPvm>
    <virta:Laajuus>
      <virta:Opintopiste>5.000000</virta:Opintopiste>
    </virta:Laajuus>
    <virta:Arvosana>
      <virta:Viisiportainen>5</virta:Viisiportainen>
    </virta:Arvosana>
    <virta:Myontaja>10076</virta:Myontaja>
    {if (organisaatio.isDefined) organisaatio.get}
    <virta:Laji>2</virta:Laji>
    <virta:Nimi>Dynamics of Structures; lectures and exercises L</virta:Nimi>
    <virta:Kieli>en</virta:Kieli>
    <virta:Koulutusala>
      <virta:Koodi versio="opm95opa">89</virta:Koodi>
      <virta:Osuus>1.000000</virta:Osuus>
    </virta:Koulutusala>
    <virta:Opinnaytetyo>0</virta:Opinnaytetyo>
    {
      if (luokittelu.isDefined) {
        <virta:Luokittelu>{luokittelu.get}</virta:Luokittelu>
      }
    }
    {
      if (!ilmanAinePätevyyksiä) {
        <virta:Patevyys>kl</virta:Patevyys>
        <virta:Patevyys>aj</virta:Patevyys>
      }
    }
    {
      if (!ilmanOpePätevyyksiä) {
        <virta:Patevyys>ob</virta:Patevyys>
      }
    }
    <virta:Patevyys>ll4</virta:Patevyys>
    <virta:Patevyys>12</virta:Patevyys>
  </virta:Opintosuoritus>

  val virtaOpiskeluoikeudet: Elem = opiskeluoikeusWithOrganisaatio(None)

  def opiskeluoikeusWithOrganisaatio(
    organisaatio: Option[Elem],
    tilallinen: Boolean = true,
    päättynyt: Boolean = false,
    luokittelu: Option[Int] = None,
    ilmanAinePätevyyksiä: Boolean = false,
    ilmanOpePätevyyksiä: Boolean = false,
    laajuudellinen: Boolean = true,
    rahoituslähde: Option[String] = Some("1")
  ): Elem = <virta:Opiskeluoikeudet>
    <virta:Opiskeluoikeus opiskelijaAvain="avopH1" avain="avopH1O1">
      <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
      {
        if(tilallinen){
          <virta:Tila>
            <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
            <virta:Koodi>1</virta:Koodi>
          </virta:Tila>
        }
      }
      {
        if (tilallinen && päättynyt) {
          <virta:Tila>
            <virta:AlkuPvm>{organisaatioVanhallaNimelläPvm.toString}</virta:AlkuPvm>
            <virta:Koodi>6</virta:Koodi>
          </virta:Tila>
        }
      }
      <virta:Tyyppi>1</virta:Tyyppi>
      <virta:Myontaja>10076</virta:Myontaja>
      {if (organisaatio.isDefined) organisaatio.get}
      <virta:Jakso koulutusmoduulitunniste="opiskeluoikeuden_kk_tunniste">
        <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
        <virta:LoppuPvm>2008-08-02</virta:LoppuPvm>
        <virta:Koulutuskoodi>621702</virta:Koulutuskoodi>
        <virta:Koulutuskunta>091</virta:Koulutuskunta>
        <virta:Koulutuskieli>en</virta:Koulutuskieli>
        {
          if (rahoituslähde.isDefined) {
            <virta:Rahoituslahde>{rahoituslähde.get}</virta:Rahoituslahde>
          }
        }
        {
          if (luokittelu.isDefined) {
            <virta:Luokittelu>{luokittelu.get}</virta:Luokittelu>
          }
        }
        <virta:Nimi kieli="fi">Nimi 1</virta:Nimi>
        <virta:Nimi kieli="sv">Nimi 1</virta:Nimi>
        <virta:Nimi kieli="en">Nimi 1</virta:Nimi>
      </virta:Jakso>
      <virta:Jakso koulutusmoduulitunniste="opiskeluoikeuden_kk_tunniste">
        <virta:AlkuPvm>2008-08-03</virta:AlkuPvm>
        <virta:Koulutuskoodi>621702</virta:Koulutuskoodi>
        <virta:Koulutuskunta>091</virta:Koulutuskunta>
        <virta:Koulutuskieli>en</virta:Koulutuskieli>
        {
          if (rahoituslähde.isDefined) {
            <virta:Rahoituslahde>{rahoituslähde.get}</virta:Rahoituslahde>
          }
        }
        {
          if (luokittelu.isDefined) {
            <virta:Luokittelu>{luokittelu.get}</virta:Luokittelu>
          }
        }
        <virta:Nimi kieli="fi">Nimi 2</virta:Nimi>
        <virta:Nimi kieli="sv">Nimi 2</virta:Nimi>
        <virta:Nimi kieli="en">Nimi 2</virta:Nimi>
        {
          if (!ilmanAinePätevyyksiä) {
            <virta:Patevyys>ew</virta:Patevyys>
          }
        }
        {
          if (!ilmanOpePätevyyksiä) {
            <virta:Patevyys>oa</virta:Patevyys>
            <virta:Patevyys>ob</virta:Patevyys>
          }
        }
        <virta:Patevyys>far</virta:Patevyys>
        <virta:Patevyys>16</virta:Patevyys>
      </virta:Jakso>
      {
        if (laajuudellinen) {
          <virta:Laajuus>
            <virta:Opintopiste>240</virta:Opintopiste>
          </virta:Laajuus>
        }
      }
    </virta:Opiskeluoikeus>
  </virta:Opiskeluoikeudet>

  def opiskeluoikeusSuorituksella(suoritusPvm: String = "2014-05-30", ilmanAinePätevyyksiä: Boolean = false, ilmanOpePätevyyksiä: Boolean = false): Elem = <virta:Opiskelija avain="lut-student-xxx">
    <virta:Henkilotunnus>xxxxxx-xxxx</virta:Henkilotunnus>
    {opiskeluoikeusWithOrganisaatio(None, tilallinen = false, ilmanAinePätevyyksiä = ilmanAinePätevyyksiä, ilmanOpePätevyyksiä = ilmanOpePätevyyksiä)}
    <virta:Opintosuoritukset>
    {suoritusWithOrganisaatio(None, suoritusPvm, luokittelu = None, ilmanAinePätevyyksiä = ilmanAinePätevyyksiä, ilmanOpePätevyyksiä = ilmanOpePätevyyksiä)}
    </virta:Opintosuoritukset>
  </virta:Opiskelija>

  def convertOpiskeluoikeusWithOrganisaatio(organisaatioXml: Option[Elem], päättynyt: Boolean = false) =
    converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(organisaatioXml, päättynyt = päättynyt)).head

  def convertOpiskeluoikeusWithOrganisaatioAndSuoritus(suoritusPvm: String = "2014-05-30") =
    converter.convertToOpiskeluoikeudet(opiskeluoikeusSuorituksella(suoritusPvm)).head

  def withArvosana(arvosana: Elem, suoritus: Elem = baseSuoritus): Elem =
    XML.copyElem(
      suoritus,
      for (subNode <- suoritus.child) yield subNode match {
        case <Arvosana>{ contents @ _* }</Arvosana> => arvosana
        case other@_                                => other
      }
    )

  def withLaajuus(laajuus: Elem, suoritus: Elem = baseSuoritus): Elem =
    XML.copyElem(
      suoritus,
      for (subNode <- suoritus.child) yield subNode match {
        case <Laajuus>{ contents @ _* }</Laajuus> => laajuus
        case other@_                              => other
      }
    )

  "Virta-opiskeluoikeuksien konvertointi" - {
    val opiskeluoikeudet = converter.convertToOpiskeluoikeudet(virtaOpiskeluoikeudet)
    "toimii" in {
      opiskeluoikeudet shouldBe a[List[_]]
      opiskeluoikeudet should have length 1
      opiskeluoikeudet.head shouldBe a[KorkeakoulunOpiskeluoikeus]
    }
    "Opiskeluoikeuden tyyppi" - {
      "sisältää koodin ja nimen" in {
        opiskeluoikeudet.head.tyyppi.koodiarvo should be ("korkeakoulutus")
        opiskeluoikeudet.head.tyyppi.nimi.value should be (LocalizedString.sanitizeRequired(Map(("fi" -> "Korkeakoulutus"), ("sv" -> "Högskoleutbildning"), "en" -> "Higher education"), "Korkeakoulutus"))
      }
    }
    "Virta-datasta saatu nimi valitaan oikein" in {
      opiskeluoikeudet.head.suoritukset.head.koulutusmoduuli.nimi.get("fi") shouldBe "Nimi 2"
    }

    "Luokittelu" - {
      "parsitaan koodistoviitteeksi jos olemassa" in {
        val opiskeluoikeudetLuokittelulla = converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(None, luokittelu=Some(3)))
        val luokittelut = opiskeluoikeudetLuokittelulla.flatMap(_.luokittelu)
        luokittelut.size should be (1)
        luokittelut.head.head.koodistoUri should be ("virtaopiskeluoikeudenluokittelu")
        luokittelut.head.head.koodiarvo should be ("3")
      }
      "on None jos ei olemassa" in {
        val opiskeluoikeudet = converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(None, luokittelu=None))
        val luokittelut = opiskeluoikeudet.flatMap(_.luokittelu)
        luokittelut should be (empty)
      }
    }

    "Vaadittu laajuus" - {
      "parsitaan opiskeluoikeudelta" in {
        convertOpiskeluoikeusWithOrganisaatio(None).lisätiedot.value
          .vaadittuLaajuus.value.arvo shouldBe 240
      }

      "on None jos opiskeluoikeudella ei ole laajuutta" in {
        converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(None, laajuudellinen = false))
          .head.lisätiedot.value.vaadittuLaajuus shouldBe None
      }
    }

    "Siirto-opiskelija" - {
      "on None jos opiskeluoikeudella ei ole SiirtoOpiskelija-elementtiä" in {
        convertOpiskeluoikeusWithOrganisaatio(None).lisätiedot.value.siirtoOpiskelija shouldBe None
      }
    }

    "Patevyys" - {
      "parsitaan vain opintosuorituksilta ilman duplikaatteja ja vain kaksimerkkiset kirjainkoodit huomioidaan" in {
        val oo = converter.convertToOpiskeluoikeudet(opiskeluoikeusSuorituksella())
        oo should have size (1)
        val opettajanPatevyydet = oo.head.lisätiedot.flatMap(_.opettajanPedagogisetOpinnot.map(x => x.map(_.koodiarvo))).getOrElse(List())
        val ainePatevyydet = oo.head.lisätiedot.flatMap(_.opetettavanAineenOpinnot.map(x => x.map(_.koodiarvo))).getOrElse(List())
        opettajanPatevyydet should equal(List("ob"))
        ainePatevyydet should equal(List("aj", "kl"))
      }

      "ainepätevyydet on None jos sopivia pätevyyksiä ei löydy" in {
        val oo = converter.convertToOpiskeluoikeudet(opiskeluoikeusSuorituksella(ilmanAinePätevyyksiä = true))
        oo should have size (1)
        val ainePatevyydet = oo.head.lisätiedot.get.opetettavanAineenOpinnot
        ainePatevyydet should equal(None)
      }

      "opettajan pätevyydet on None jos sopivia pätevyyksiä ei löydy" in {
        val oo = converter.convertToOpiskeluoikeudet(opiskeluoikeusSuorituksella(ilmanOpePätevyyksiä = true))
        oo should have size (1)
        val opePatevyydet = oo.head.lisätiedot.get.opettajanPedagogisetOpinnot
        opePatevyydet should equal(None)
      }
    }

    "Rahoituslähde" - {
      def rahoituslähdeJaksot(rahoituslähde: Option[String]) =
        converter
          .convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(None, rahoituslähde = rahoituslähde))
          .head
          .lisätiedot
          .get
          .rahoituslähdeJaksot

      "parsitaan jaksoittain alkupäivän mukaan järjestettynä" in {
        val jaksot = rahoituslähdeJaksot(Some("1")).get
        jaksot should have length 2

        jaksot.head.alku should be(LocalDate.of(2008, 8, 1))
        jaksot.head.loppu should be(Some(LocalDate.of(2008, 8, 2)))
        jaksot.head.rahoituslähde.koodiarvo should be("1")
        jaksot.head.rahoituslähde.koodistoUri should be("virtarahoituslahde")

        jaksot(1).alku should be(LocalDate.of(2008, 8, 3))
        jaksot(1).loppu should be(None)
        jaksot(1).rahoituslähde.koodiarvo should be("1")
      }

      "on None jos yhdelläkään jaksolla ei ole rahoituslähdettä" in {
        rahoituslähdeJaksot(None) should be(None)
      }

      "tuntematon koodiarvo ohitetaan eikä konversio kaadu" in {
        rahoituslähdeJaksot(Some("99")) should be(None)
      }
    }

    "Lähdeorganisaatio" - {

      "kun opiskeluoikeudella ei ole lähdeorganisaatiota" in {
        val opiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(None)
         opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }

      "kun opiskeluoikeudella on lähdeorganisaatio" in {
        val opiskeluoikeus: KorkeakoulunOpiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>3</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Helsingin yliopisto", Some("Helsingfors universitet"), Some("University of Helsinki")))
      }

      "kun opiskeluoikeudella on fuusioitunut myöntäjä organisaatio" in {
        val opiskeluoikeus: KorkeakoulunOpiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>5</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Helsingin yliopisto", Some("Helsingfors universitet"), Some("University of Helsinki")))
      }

      "kun opiskeluoikeuden lähdeorganisaatio on kuraa" in {
        val opiskeluoikeus: KorkeakoulunOpiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>3</virta:Rooli>
            <virta:Koodi>kuraa</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }

      "kun opiskeluoikeudella on joku muu organisaatio kuin lähdeorganisaatio" in {
        val opiskeluoikeus: KorkeakoulunOpiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>8</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }
    }
  }

  "Oppilaitoksen nimi" - {

    "haetaan viimeisimmän vahvistetun suorituksen päivämäärällä kun opiskeluoikeudella ole tiloja" in {
      val opiskeluoikeus = convertOpiskeluoikeusWithOrganisaatioAndSuoritus(suoritusPvm = organisaatioVanhallaNimelläPvm.toString)
      opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Aalto-yliopisto -vanha", Some("Aalto-universitetet -vanha"), Some("Aalto University -vanha")))
    }

    "haetaan opiskeluoikeuden päättymisen päivämäärällä" in {
      val opiskeluoikeus = convertOpiskeluoikeusWithOrganisaatio(None, päättynyt = true)
      opiskeluoikeus.oppilaitos.get.nimi.get should be(Finnish("Aalto-yliopisto -vanha", Some("Aalto-universitetet -vanha"), Some("Aalto University -vanha")))
      opiskeluoikeus.suoritukset.head.toimipiste.nimi.get should be(Finnish("Aalto-yliopisto -vanha", Some("Aalto-universitetet -vanha"), Some("Aalto University -vanha")))
      opiskeluoikeus.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt shouldBe true
      opiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo shouldBe "6"
      opiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.nimi.get.get("fi") shouldBe "määräaika päättynyt"
    }

  }



  "Suoritusten konvertointi" - {
    "Suorituskieli" - {
      "parsitaan tutkinnon suoritukselta" in {
        val suoritus = convertSuoritus(tutkintoSuoritus(kieli = Some("sv")))
        suoritus shouldBe defined
        suoritus.get shouldBe a[KorkeakoulututkinnonSuoritus]
        suoritus.get.suorituskieli shouldBe defined
        suoritus.get.suorituskieli.get.koodiarvo shouldBe "SV"
        suoritus.get.suorituskieli.get.koodistoUri shouldBe "kieli"
      }

      "parsitaan opintojakson suoritukselta" in {
        val suoritus = convertSuoritus(baseSuoritus)
        suoritus shouldBe defined
        suoritus.get shouldBe a[KorkeakoulunOpintojaksonSuoritus]
        suoritus.get.suorituskieli shouldBe defined
        suoritus.get.suorituskieli.get.koodiarvo shouldBe "EN"
      }

      "on None jos Kieli-elementti puuttuu tutkinnon suoritukselta" in {
        val suoritus = convertSuoritus(tutkintoSuoritus(kieli = None))
        suoritus shouldBe defined
        suoritus.get shouldBe a[KorkeakoulututkinnonSuoritus]
        suoritus.get.suorituskieli shouldBe None
      }
    }

    "Hyväksilukupäivä" - {
      def opintojaksoWithHyvaksiluku(hyvaksilukuPvm: Option[String]): Elem =
        <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="K-123" avain="hyvaksiluku-1">
          <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
          <virta:Laajuus>
            <virta:Opintopiste>5</virta:Opintopiste>
          </virta:Laajuus>
          <virta:Arvosana>
            <virta:Viisiportainen>5</virta:Viisiportainen>
          </virta:Arvosana>
          <virta:Myontaja>10076</virta:Myontaja>
          <virta:Laji>2</virta:Laji>
          <virta:Nimi kieli="fi">Hyväksiluettu opintojakso</virta:Nimi>
          <virta:Kieli>fi</virta:Kieli>
          {hyvaksilukuPvm.map(pvm => <virta:HyvaksilukuPvm>{pvm}</virta:HyvaksilukuPvm>).getOrElse(scala.xml.NodeSeq.Empty)}
        </virta:Opintosuoritus>

      def tutkintoWithHyvaksiluku(hyvaksilukuPvm: Option[String]): Elem =
        <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="tutkinto-123" avain="tutkinto-hyvaksiluku-1">
          <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
          <virta:Laajuus>
            <virta:Opintopiste>180</virta:Opintopiste>
          </virta:Laajuus>
          <virta:Arvosana>
            <virta:Hyvaksytty>HYV</virta:Hyvaksytty>
          </virta:Arvosana>
          <virta:Myontaja>10076</virta:Myontaja>
          <virta:Laji>1</virta:Laji>
          <virta:Nimi kieli="fi">Hyväksiluettu tutkinto</virta:Nimi>
          <virta:Kieli>fi</virta:Kieli>
          <virta:Koulutuskoodi>612103</virta:Koulutuskoodi>
          {hyvaksilukuPvm.map(pvm => <virta:HyvaksilukuPvm>{pvm}</virta:HyvaksilukuPvm>).getOrElse(scala.xml.NodeSeq.Empty)}
        </virta:Opintosuoritus>

      "parsitaan opintojakson suoritukselta" in {
        val suoritus = convertSuoritus(opintojaksoWithHyvaksiluku(Some("2018-03-16")))
        suoritus shouldBe defined
        suoritus.get shouldBe a[KorkeakoulunOpintojaksonSuoritus]
        suoritus.get.asInstanceOf[KorkeakoulunOpintojaksonSuoritus].hyväksilukupäivä shouldBe Some(LocalDate.of(2018, 3, 16))
      }

      "parsitaan tutkinnon suoritukselta" in {
        val suoritus = convertSuoritus(tutkintoWithHyvaksiluku(Some("2018-03-16")))
        suoritus shouldBe defined
        suoritus.get shouldBe a[KorkeakoulututkinnonSuoritus]
        suoritus.get.asInstanceOf[KorkeakoulututkinnonSuoritus].hyväksilukupäivä shouldBe Some(LocalDate.of(2018, 3, 16))
      }

      "on None jos HyvaksilukuPvm puuttuu" in {
        val suoritus = convertSuoritus(opintojaksoWithHyvaksiluku(None))
        suoritus shouldBe defined
        suoritus.get.asInstanceOf[KorkeakoulunOpintojaksonSuoritus].hyväksilukupäivä shouldBe None
      }
    }

    "Opinnäytetyö" - {
      def opintojaksoWithOpinnaytetyo(arvo: Option[String]): Elem =
        <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="K-124" avain="opinnayte-1">
          <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
          <virta:Laajuus>
            <virta:Opintopiste>5</virta:Opintopiste>
          </virta:Laajuus>
          <virta:Arvosana>
            <virta:Viisiportainen>5</virta:Viisiportainen>
          </virta:Arvosana>
          <virta:Myontaja>10076</virta:Myontaja>
          <virta:Laji>2</virta:Laji>
          <virta:Nimi kieli="fi">Diplomityö</virta:Nimi>
          <virta:Kieli>fi</virta:Kieli>
          {arvo.map(a => <virta:Opinnaytetyo>{a}</virta:Opinnaytetyo>).getOrElse(scala.xml.NodeSeq.Empty)}
        </virta:Opintosuoritus>

      def opinnäytetyö(arvo: Option[String]): Option[Boolean] =
        convertSuoritus(opintojaksoWithOpinnaytetyo(arvo))
          .value.asInstanceOf[KorkeakoulunOpintojaksonSuoritus].opinnäytetyö

      "arvo 1 tulkitaan todeksi" in {
        opinnäytetyö(Some("1")) shouldBe Some(true)
      }

      "arvo true tulkitaan todeksi" in {
        opinnäytetyö(Some("true")) shouldBe Some(true)
      }

      "arvo 0 tulkitaan epätodeksi" in {
        opinnäytetyö(Some("0")) shouldBe Some(false)
      }

      "on None jos Opinnaytetyo puuttuu" in {
        opinnäytetyö(None) shouldBe None
      }
    }

    "Julkinen lisätieto" - {
      def opintojaksoWithLisatieto(lisatiedot: Seq[Elem]): Elem =
        <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="K-125" avain="lisatieto-1">
          <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
          <virta:Laajuus>
            <virta:Opintopiste>5</virta:Opintopiste>
          </virta:Laajuus>
          <virta:Arvosana>
            <virta:Viisiportainen>5</virta:Viisiportainen>
          </virta:Arvosana>
          <virta:Myontaja>10076</virta:Myontaja>
          <virta:Laji>2</virta:Laji>
          <virta:Nimi kieli="fi">Opintojakso</virta:Nimi>
          <virta:Kieli>fi</virta:Kieli>
          {lisatiedot}
        </virta:Opintosuoritus>

      def lisätieto(lisatiedot: Seq[Elem]): Option[LocalizedString] =
        convertSuoritus(opintojaksoWithLisatieto(lisatiedot))
          .value.asInstanceOf[KorkeakoulunOpintojaksonSuoritus].lisätieto

      "parsitaan kielitagittomasta elementistä" in {
        lisätieto(Seq(<virta:JulkinenLisatieto>Theory of Elasticity</virta:JulkinenLisatieto>))
          .map(_.get("fi")) shouldBe Some("Theory of Elasticity")
      }

      "parsitaan kielitagillisesta elementistä" in {
        lisätieto(Seq(<virta:JulkinenLisatieto kieli="fi">Talouskriisit</virta:JulkinenLisatieto>))
          .map(_.get("fi")) shouldBe Some("Talouskriisit")
      }

      "on None jos JulkinenLisatieto puuttuu" in {
        lisätieto(Nil) shouldBe None
      }

      "SalainenLisatieto-elementtiä ei lueta" in {
        lisätieto(Seq(<virta:SalainenLisatieto>Ei saa näkyä</virta:SalainenLisatieto>)) shouldBe None
      }

      "parsitaan myös tutkinnon suoritukselta" in {
        val tutkinto =
          <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="tutkinto-125" avain="tutkinto-lisatieto-1">
            <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
            <virta:Laajuus><virta:Opintopiste>180</virta:Opintopiste></virta:Laajuus>
            <virta:Arvosana><virta:Hyvaksytty>HYV</virta:Hyvaksytty></virta:Arvosana>
            <virta:Myontaja>10076</virta:Myontaja>
            <virta:Laji>1</virta:Laji>
            <virta:Nimi kieli="fi">Tutkinto</virta:Nimi>
            <virta:Koulutuskoodi>612103</virta:Koulutuskoodi>
            <virta:JulkinenLisatieto kieli="fi">Yleistä lisätietoa</virta:JulkinenLisatieto>
          </virta:Opintosuoritus>
        convertSuoritus(tutkinto).value.asInstanceOf[KorkeakoulututkinnonSuoritus]
          .lisätieto.map(_.get("fi")) shouldBe Some("Yleistä lisätietoa")
      }
    }

    "Luokittelu" - {
      "parsitaan koodistoviitteeksi" in {
        val luokittelut = convertSuoritus(suoritusWithOrganisaatio(None, luokittelu=Some(1)))
          .flatMap {
            case x: KorkeakoulunOpintojaksonSuoritus => {
              x.luokittelu
            }
            case _ => None
          }
        luokittelut.size should be (1)
        luokittelut.head.head.koodistoUri should be("virtaopsuorluokittelu")
        luokittelut.head.head.koodiarvo should be("1")
      }
      "on None jos ei löydy XML:stä" in {
        val luokittelut = convertSuoritus(suoritusWithOrganisaatio(None, luokittelu=None))
          .flatMap {
            case x: KorkeakoulunOpintojaksonSuoritus => {
              x.luokittelu
            }
            case _ => None
          }
        luokittelut should be (empty)
      }
      "ei-numeerinen arvo sivuutetaan eikä kaadeta konversiota" in {
        val suoritus =
          <virta:Opintosuoritus opiskeluoikeusAvain="avopH1O1" opiskelijaAvain="avopH1" koulutusmoduulitunniste="K-126" avain="luokittelu-1">
            <virta:SuoritusPvm>2017-12-04</virta:SuoritusPvm>
            <virta:Laajuus><virta:Opintopiste>5</virta:Opintopiste></virta:Laajuus>
            <virta:Arvosana><virta:Viisiportainen>5</virta:Viisiportainen></virta:Arvosana>
            <virta:Myontaja>10076</virta:Myontaja>
            <virta:Laji>2</virta:Laji>
            <virta:Nimi kieli="fi">Opintojakso</virta:Nimi>
            <virta:Luokittelu>b</virta:Luokittelu>
          </virta:Opintosuoritus>
        convertSuoritus(suoritus)
          .value.asInstanceOf[KorkeakoulunOpintojaksonSuoritus].luokittelu shouldBe None
      }
    }

    "Lähdeorganisaatio" - {
      def covertSuoritusWithOrganisaatio(organisaatioXml: Option[Elem]) =
        convertSuoritus(suoritusWithOrganisaatio(organisaatioXml)).get

      "kun suorituksella ei ole lähdeorganisaatiota" in {
        val suoritus = covertSuoritusWithOrganisaatio(None)
        suoritus.toimipiste.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }

      "kun suorituksella on lähdeorganisaatio" in {
        val suoritus = covertSuoritusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>3</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        suoritus.toimipiste.nimi.get should be(Finnish("Helsingin yliopisto", Some("Helsingfors universitet"), Some("University of Helsinki")))
      }

      "kun suorituksella on fuusioitunut myöntäjä organisaatio" in {
        val suoritus = covertSuoritusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>5</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        suoritus.toimipiste.nimi.get should be(Finnish("Helsingin yliopisto", Some("Helsingfors universitet"), Some("University of Helsinki")))
      }

      "kun suorituksella on joku muu organisaatio kuin lähdeorganisaatio" in {
        val suoritus = covertSuoritusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>9</virta:Rooli>
            <virta:Koodi>01901</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        suoritus.toimipiste.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }

      "kun suorituksella lähdeorganisaatio on kuraa" in {
        val suoritus = covertSuoritusWithOrganisaatio(Some(
          <virta:Organisaatio>
            <virta:Rooli>3</virta:Rooli>
            <virta:Koodi>kuraa</virta:Koodi>
            <virta:Osuus>1</virta:Osuus>
          </virta:Organisaatio>
        ))

        suoritus.toimipiste.nimi.get should be(Finnish("Aalto-yliopisto", Some("Aalto-universitetet"), Some("Aalto University")))
      }
    }

    "Laajuudet" - {
      def convertLaajuus(laajuus: Elem): Laajuus = convertSuoritus(withLaajuus(laajuus)).flatMap(_.koulutusmoduuli.getLaajuus).get

      "Opintoviikkoina, jos laajuutta ei annettu opintopisteinä" in {
        convertLaajuus(<virta:Laajuus>
          <virta:Opintoviikko>2.0</virta:Opintoviikko>
        </virta:Laajuus>) should equal(LaajuusOpintoviikoissa(2.0, laajuusOpintoviikoissa))
      }

      "Opintoviikkoina, jos laajuus opintopisteinä 0" in {
        convertLaajuus(<virta:Laajuus>
          <virta:Opintopiste>0.0</virta:Opintopiste>
          <virta:Opintoviikko>2.0</virta:Opintoviikko>
        </virta:Laajuus>) should equal(LaajuusOpintoviikoissa(2.0, laajuusOpintoviikoissa))
      }

      "Opintopisteinä, jos opintoviikkoa ei annettu" in {
        convertLaajuus(<virta:Laajuus>
          <virta:Opintopiste>2.0</virta:Opintopiste>
        </virta:Laajuus>) should equal(LaajuusOpintopisteissä(2.0, laajuusOpintopisteissä))
      }

      "Opintopisteinä, jos opintoviikot ja opintopisteet annettu" in {
        convertLaajuus(<virta:Laajuus>
          <virta:Opintoviikko>2.0</virta:Opintoviikko>
          <virta:Opintopiste>2.0</virta:Opintopiste>
        </virta:Laajuus>) should equal(LaajuusOpintopisteissä(2.0, laajuusOpintopisteissä))
      }
    }

    "Arviointi" - {
      def convertArviointi(arvosana: Elem): Arviointi = convertSuoritus(withArvosana(arvosana)).flatMap(_.arviointi).flatMap(_.headOption).get
      "Viisiportainen" - {
        "numero" in {
          (convertArviointi(<virta:Arvosana>
            <virta:Viisiportainen>3</virta:Viisiportainen>
          </virta:Arvosana>)
            should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("3", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
        }
        "hyväksytty" in {
          (convertArviointi(<virta:Arvosana><virta:Viisiportainen>HYV</virta:Viisiportainen></virta:Arvosana>)
            should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("HYV", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
        }
      }
      "ToinenKotimainen" in {
        (convertArviointi(<virta:Arvosana><virta:ToinenKotimainen>HT</virta:ToinenKotimainen></virta:Arvosana>)
          should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("HT", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
      }
      "Hyvaksytty" in {
        (convertArviointi(<virta:Arvosana><virta:Hyvaksytty>HYV</virta:Hyvaksytty></virta:Arvosana>)
          should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("HYV", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
      }
      "Naytetyo" in {
        (convertArviointi(<virta:Arvosana><virta:Naytetyo>KH</virta:Naytetyo>KH</virta:Arvosana>)
          should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("KH", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
      }
      "Tutkielma" in {
        (convertArviointi(<virta:Arvosana><virta:Tutkielma>C</virta:Tutkielma></virta:Arvosana>)
          should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("C", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
      }
      "EiKaytossa" in {
        (convertArviointi(<virta:Arvosana><virta:EiKaytossa>Arvosana ei kaytossa</virta:EiKaytossa></virta:Arvosana>)
          should equal(KorkeakoulunKoodistostaLöytyväArviointi(Koodistokoodiviite("Arvosana ei kaytossa", "virtaarvosana"), LocalDate.of(2014, 5, 30))))
      }
      "Muu" - {
        "Aalto - Oivallisesti" in {
          val arvosana =
            <virta:Arvosana>
              <virta:Muu>
                <virta:Asteikko avain="310">
                  <virta:Nimi>OIV
                    &#xB7;
                    Perustutkinnot</virta:Nimi>
                  <virta:AsteikkoArvosana avain="310">
                    <virta:Koodi>HYL</virta:Koodi>
                    <virta:LaskennallinenArvo>0.000000</virta:LaskennallinenArvo>
                  </virta:AsteikkoArvosana>
                  <virta:AsteikkoArvosana avain="311">
                    <virta:Koodi>OIV</virta:Koodi>
                    <virta:LaskennallinenArvo>0.000000</virta:LaskennallinenArvo>
                  </virta:AsteikkoArvosana>
                  <virta:AsteikkoArvosana avain="319">
                    <virta:Koodi>HYV</virta:Koodi>
                    <virta:LaskennallinenArvo>0.000000</virta:LaskennallinenArvo>
                  </virta:AsteikkoArvosana>
                </virta:Asteikko>
                <virta:Koodi>311</virta:Koodi>
              </virta:Muu>
            </virta:Arvosana>
          (convertArviointi(arvosana)
            should equal(KorkeakoulunPaikallinenArviointi(KorkeakoulunPaikallinenArvosana("OIV", "OIV", Some("virta/310")), LocalDate.of(2014, 5, 30))))
        }
        "Hanken - Poäng" in {
          val arvosana =
            <virta:Arvosana>
              <virta:Muu>
                <virta:Asteikko avain="4">
                  <virta:Nimi>po&#xE4;ng (0-100), godk&#xE4;nd</virta:Nimi>
                  <virta:AsteikkoArvosana avain="2006715">
                    <virta:Koodi>76</virta:Koodi>
                    <virta:Nimi>76</virta:Nimi>
                    <virta:LaskennallinenArvo>76.0</virta:LaskennallinenArvo>
                  </virta:AsteikkoArvosana>
                </virta:Asteikko>
                <virta:Koodi>2006715</virta:Koodi>
              </virta:Muu>
            </virta:Arvosana>
          (convertArviointi(arvosana)
            should equal(KorkeakoulunPaikallinenArviointi(KorkeakoulunPaikallinenArvosana("76", "76", Some("virta/4")), LocalDate.of(2014, 5, 30))))
        }
      }
    }
  }

  "Mock-datasta konvertointi" - {
    "hyväksilukuPäivä parsitaan oikein mock-datasta (090992-3237.xml)" in {
      val xmlString = Files.asString("src/main/resources/mockdata/virta/opintotiedot/090992-3237.xml").get
      val xml = scala.xml.XML.loadString(xmlString)
      val opiskeluoikeudet = converter.convertToOpiskeluoikeudet(xml)

      // Tiedostossa on opintojakso avaimella "313405" jolla on HyvaksilukuPvm 2014-12-16
      val hyväksiluetutOpintojaksot = opiskeluoikeudet
        .flatMap(_.suoritukset)
        .flatMap {
          case s: KorkeakoulunOpintojaksonSuoritus => Some(s)
          case _ => None
        }
        .filter(_.hyväksilukupäivä.isDefined)

      hyväksiluetutOpintojaksot should not be empty
      hyväksiluetutOpintojaksot.exists(_.hyväksilukupäivä.contains(LocalDate.of(2014, 12, 16))) shouldBe true
    }

    "ilmoittautumispäivä parsitaan oikein mock-datasta (170691-3962.xml)" in {
      val xmlString = Files.asString("src/main/resources/mockdata/virta/opintotiedot/170691-3962.xml").get
      val xml = scala.xml.XML.loadString(xmlString)
      val jaksot = converter.convertToOpiskeluoikeudet(xml)
        .flatMap(_.lisätiedot.toList)
        .flatMap(_.lukukausiIlmoittautuminen.toList)
        .flatMap(_.ilmoittautumisjaksot)

      jaksot.map(_.ilmoittautumispäivä) shouldBe List(Some(LocalDate.of(2013, 7, 10)), Some(LocalDate.of(2013, 7, 10)))
      jaksot.map(_.alku) shouldBe List(LocalDate.of(2013, 8, 1), LocalDate.of(2014, 1, 1))
    }

    "siirto-opiskelijan tiedot parsitaan oikein mock-datasta (141199-418X.xml)" in {
      val xmlString = Files.asString("src/main/resources/mockdata/virta/opintotiedot/141199-418X.xml").get
      val xml = scala.xml.XML.loadString(xmlString)
      val opiskeluoikeudet = converter.convertToOpiskeluoikeudet(xml)

      val siirrot = opiskeluoikeudet.flatMap(_.lisätiedot).flatMap(_.siirtoOpiskelija)
      siirrot should have length 1
      siirrot.head.siirtoPäivä shouldBe LocalDate.of(2017, 1, 2)
      siirrot.head.lähdeOrganisaatio shouldBe defined
    }

    "liikkuvuusjaksot" - {
      def opiskeluoikeudet(tiedosto: String): List[KorkeakoulunOpiskeluoikeus] = {
        val xmlString = Files.asString(s"src/main/resources/mockdata/virta/opintotiedot/$tiedosto").get
        converter.convertToOpiskeluoikeudet(scala.xml.XML.loadString(xmlString))
      }

      def liikkuvuusjaksot(oo: KorkeakoulunOpiskeluoikeus): List[Liikkuvuusjakso] =
        oo.lisätiedot.toList.flatMap(_.liikkuvuusjaksot.toList.flatten)

      "kohdistetaan opiskeluoikeusavaimella (090802A952F.xml)" in {
        val oot = opiskeluoikeudet("090802A952F.xml")
        oot should have length 1

        val jaksot = liikkuvuusjaksot(oot.head)
        jaksot.map(_.alku) shouldBe List(
          LocalDate.of(2025, 3, 5),
          LocalDate.of(2025, 11, 10),
          LocalDate.of(2026, 3, 9)
        )
        jaksot.map(_.loppu) shouldBe List(
          Some(LocalDate.of(2025, 5, 22)),
          Some(LocalDate.of(2025, 11, 14)),
          Some(LocalDate.of(2026, 3, 13))
        )
        jaksot.map(_.maa.koodiarvo) shouldBe List("620", "528", "056")
        jaksot.map(_.liikkuvuusohjelma.koodiarvo) shouldBe List("108", "108", "101")
        jaksot.map(_.suunta.koodiarvo).distinct shouldBe List("1")
        jaksot.map(_.tyyppi.koodiarvo).distinct shouldBe List("1")

        jaksot.map(_.luokittelu.toList.flatten.map(_.koodiarvo)) shouldBe List(Nil, Nil, List("b"))
        jaksot.last.luokittelu.get.head.koodistoUri shouldBe "liikkuvuudenluokittelu"
      }

      "maakoodin etunollat säilyvät (090802A952F.xml)" in {
        val maat = opiskeluoikeudet("090802A952F.xml").flatMap(liikkuvuusjaksot).map(_.maa.koodiarvo)
        maat should contain("056")
        maat should not contain "56"
      }

      "kohdistetaan oikealle opiskeluoikeudelle kun opiskeluoikeuksia on useita (060180-9521.xml)" in {
        val oot = opiskeluoikeudet("060180-9521.xml")
        oot should have length 2

        val (avaimellinen, muut) = oot.partition(_.lähdejärjestelmänId.flatMap(_.id).contains("1203130"))
        avaimellinen should have length 1
        liikkuvuusjaksot(avaimellinen.head).map(j => (j.alku, j.maa.koodiarvo, j.liikkuvuusohjelma.koodiarvo)) shouldBe List(
          (LocalDate.of(2014, 5, 4), "276", "106"),
          (LocalDate.of(2014, 9, 1), "410", "106")
        )
        muut.flatMap(liikkuvuusjaksot) shouldBe Nil
      }

      "kohdistetaan myöntäjän ja voimassaolon perusteella kun opiskeluoikeusavain puuttuu (030199-3419.xml)" in {
        val jaksot = opiskeluoikeudet("030199-3419.xml").flatMap(liikkuvuusjaksot)

        jaksot should have length 1
        jaksot.head.alku shouldBe LocalDate.of(2001, 7, 15)
        jaksot.head.loppu shouldBe Some(LocalDate.of(2001, 8, 7))
        jaksot.head.maa.koodiarvo shouldBe "752"
        jaksot.head.liikkuvuusohjelma.koodiarvo shouldBe "107"
      }

      "fuusiosta syntyneet kaksoiskappaleet karsitaan (020276-901K.xml)" in {
        val xmlString = Files.asString("src/main/resources/mockdata/virta/opintotiedot/020276-901K.xml").get
        val xml = scala.xml.XML.loadString(xmlString)
        val liikkuvuusjaksoNodet = (xml \\ "Liikkuvuusjakso").toList

        // Neljä elementtiä, mutta vain kaksi eri avainta: sama jakso siirtyy sekä vanhan (01905) että
        // uuden (10122) myöntäjän alla.
        liikkuvuusjaksoNodet should have length 4
        liikkuvuusjaksoNodet.map(n => (n \ "@avain").text).distinct should have length 2

        val oot = converter.convertToOpiskeluoikeudet(xml)

        // Yksikään opiskeluoikeus ei saa saada samaa jaksoa kahdesti: fuusioituneen korkeakoulun
        // opiskeluoikeus täsmää sekä vanhaan että uuteen myöntäjään, jolloin molemmat kopiot osuisivat
        // siihen ilman avaimeen perustuvaa karsintaa.
        oot.foreach(oo => liikkuvuusjaksot(oo).distinct shouldBe liikkuvuusjaksot(oo))

        // Fuusiodatassa myös opiskeluoikeudet itsessään siirtyvät kahteen kertaan (sama avain vanhan ja
        // uuden myöntäjän alla). Ne yhdistää VirtaOpiskeluoikeusRepository.virtaHaku .distinctillä, joten
        // oppijalle päätyvät liikkuvuusjaksot lasketaan samalla tavalla.
        val jaksot = oot.distinct.flatMap(liikkuvuusjaksot)
        jaksot should have length 2
        jaksot.map(j => (j.alku, j.maa.koodiarvo)).sortBy(_._1.toString) shouldBe List(
          (LocalDate.of(2011, 8, 23), "752"),
          (LocalDate.of(2013, 9, 1), "724")
        )
      }
    }
  }
}
