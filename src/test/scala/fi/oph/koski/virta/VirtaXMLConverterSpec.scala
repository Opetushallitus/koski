package fi.oph.koski.virta

import fi.oph.koski.TestEnvironment
import fi.oph.koski.documentation.ExampleData.{laajuusOpintopisteissä, laajuusOpintoviikoissa}
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppilaitos.MockOppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema._
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
  def suoritusWithOrganisaatio(
    organisaatio: Option[Elem],
    suoritusPvm: String = "2014-05-30",
    luokittelu: Option[Int] = None
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
  </virta:Opintosuoritus>

  val virtaOpiskeluoikeudet: Elem = opiskeluoikeusWithOrganisaatio(None)

  def opiskeluoikeusWithOrganisaatio(
    organisaatio: Option[Elem],
    tilallinen: Boolean = true,
    päättynyt: Boolean = false,
    luokittelu: Option[Int] = None
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
        <virta:Rahoituslahde>1</virta:Rahoituslahde>
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
        <virta:Rahoituslahde>1</virta:Rahoituslahde>
        {
          if (luokittelu.isDefined) {
            <virta:Luokittelu>{luokittelu.get}</virta:Luokittelu>
          }
        }
        <virta:Nimi kieli="fi">Nimi 2</virta:Nimi>
        <virta:Nimi kieli="sv">Nimi 2</virta:Nimi>
        <virta:Nimi kieli="en">Nimi 2</virta:Nimi>
      </virta:Jakso>
      <virta:Laajuus>
        <virta:Opintopiste>240</virta:Opintopiste>
      </virta:Laajuus>
    </virta:Opiskeluoikeus>
  </virta:Opiskeluoikeudet>

  def opiskeluoikeusSuorituksella(suoritusPvm: String = "2014-05-30"): Elem = <virta:Opiskelija avain="lut-student-xxx">
    <virta:Henkilotunnus>xxxxxx-xxxx</virta:Henkilotunnus>
    {opiskeluoikeusWithOrganisaatio(None, tilallinen = false)}
    <virta:Opintosuoritukset>
    {suoritusWithOrganisaatio(None, suoritusPvm)}
    </virta:Opintosuoritukset>
  </virta:Opiskelija>

  def convertOpiskeluoikeusWithOrganisaatio(organisaatioXml: Option[Elem], päättynyt: Boolean = false) =
    converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(organisaatioXml, päättynyt = päättynyt)).head

  def convertOpiskeluoikeusWithOrganisaatioAndSuoritus(suoritusPvm: String = "2014-05-30") =
    converter.convertToOpiskeluoikeudet(opiskeluoikeusSuorituksella(suoritusPvm)).head

  def withArvosana(arvosana: Elem, suoritus: Elem = baseSuoritus): Elem = suoritus.copy(child = for (subNode <- suoritus.child) yield subNode match {
    case <Arvosana>{ contents @ _* }</Arvosana> => arvosana
    case other@_ => other
  })

  def withLaajuus(laajuus: Elem, suoritus: Elem = baseSuoritus): Elem = suoritus.copy(child = for (subNode <- suoritus.child) yield subNode match {
    case <Laajuus>{ contents @ _* }</Laajuus> => laajuus
    case other@_ => other
  })

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
      opiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.nimi.get.get("fi") shouldBe "päättynyt"
    }

  }



  "Suoritusten konvertointi" - {
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
}
