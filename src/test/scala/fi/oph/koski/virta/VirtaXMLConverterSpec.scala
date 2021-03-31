package fi.oph.koski.virta

import java.time.LocalDate

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.oppilaitos.MockOppilaitosRepository
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import org.scalatest.{FreeSpec, Matchers, OptionValues}

import scala.xml.Elem

class VirtaXMLConverterSpec extends FreeSpec with Matchers with OptionValues {

  val converter = VirtaXMLConverter(MockOppilaitosRepository, MockKoodistoViitePalvelu, MockOrganisaatioRepository)
  private def convertSuoritus(suoritus: Elem) = converter.convertSuoritus(None, suoritus, List(suoritus))

  def baseSuoritus: Elem = suoritusWithOrganisaatio(None)
  def suoritusWithOrganisaatio(organisaatio: Option[Elem]): Elem = <virta:Opintosuoritus valtakunnallinenKoulutusmoduulitunniste="" opiskeluoikeusAvain="1114082125" opiskelijaAvain="1114082124" koulutusmoduulitunniste="Kul-49.3400" avain="1114935190">
    <virta:SuoritusPvm>2014-05-30</virta:SuoritusPvm>
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
  </virta:Opintosuoritus>

  val virtaOpiskeluoikeudet: Elem = opiskeluoikeusWithOrganisaatio(None)

  def opiskeluoikeusWithOrganisaatio(organisaatio: Option[Elem]): Elem = <virta:Opiskeluoikeudet>
    <virta:Opiskeluoikeus opiskelijaAvain="avopH1" avain="avopH1O1">
      <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
      <virta:Tila>
        <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
        <virta:Koodi>1</virta:Koodi>
      </virta:Tila>
      <virta:Tyyppi>1</virta:Tyyppi>
      <virta:Myontaja>10076</virta:Myontaja>
      {if (organisaatio.isDefined) organisaatio.get}
      <virta:Jakso koulutusmoduulitunniste="opiskeluoikeuden_kk_tunniste">
        <virta:AlkuPvm>2008-08-01</virta:AlkuPvm>
        <virta:Koulutuskoodi>621702</virta:Koulutuskoodi>
        <virta:Koulutuskunta>091</virta:Koulutuskunta>
        <virta:Koulutuskieli>en</virta:Koulutuskieli>
        <virta:Rahoituslahde>1</virta:Rahoituslahde>
        <virta:Luokittelu>3</virta:Luokittelu>
      </virta:Jakso>
      <virta:Laajuus>
        <virta:Opintopiste>240</virta:Opintopiste>
      </virta:Laajuus>
    </virta:Opiskeluoikeus>
  </virta:Opiskeluoikeudet>

  def withArvosana(arvosana: Elem, suoritus: Elem = baseSuoritus): Elem = suoritus.copy(child = for (subNode <- suoritus.child) yield subNode match {
    case <Arvosana>{ contents @ _* }</Arvosana> => arvosana
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
        opiskeluoikeudet.head.tyyppi.nimi.value should be (LocalizedString.sanitizeRequired(Map(("fi" -> "Korkeakoulutus"), ("sv" -> "Högskoleutbildning")), "Korkeakoulutus"))
      }
    }

    "Lähdeorganisaatio" - {
      def convertOpiskeluoikeusWithOrganisaatio(organisaatioXml: Option[Elem]) =
        converter.convertToOpiskeluoikeudet(opiskeluoikeusWithOrganisaatio(organisaatioXml)).head

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

  "Suoritusten konvertointi" - {
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
            should equal(KorkeakoulunPaikallinenArviointi(PaikallinenKoodi("OIV", "OIV", Some("virta/310")), LocalDate.of(2014, 5, 30))))
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
            should equal(KorkeakoulunPaikallinenArviointi(PaikallinenKoodi("76", "76", Some("virta/4")), LocalDate.of(2014, 5, 30))))
        }
      }
    }
  }
}
