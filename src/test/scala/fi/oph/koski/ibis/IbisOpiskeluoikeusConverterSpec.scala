package fi.oph.koski.ibis

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.ressunLukio
import fi.oph.koski.log.RootLogTester
import fi.oph.koski.schema._
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.io.Source

class IbisOpiskeluoikeusConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private def rivitTiedostosta(nimi: String): List[IbisRecord] =
    IbisCsvParser.parse(Source.fromInputStream(getClass.getResourceAsStream(s"/ibis/$nimi"), "UTF-8").mkString)._2

  private lazy val rivit = rivitTiedostosta("esimerkki.csv")

  private val converter = new IbisOpiskeluoikeusConverter(
    KoskiApplicationForTests.koodistoViitePalvelu,
    KoskiApplicationForTests.koskiLocalizationRepository,
    KoskiApplicationForTests.validatingAndResolvingExtractor,
  )

  private val kohde = IbisKohde("1.2.246.562.24.00000000001", ressunLukio, None)
  private val vahvistuspäivä = LocalDate.of(2016, 7, 6)

  private def rivi(personalCode: String): IbisRecord = rivit.find(_.personalCode == personalCode).get

  private def coreOppiaineet(oo: IBDiplomaOpiskeluoikeus): List[IBDiplomanCoreOppiaine] =
    oo.suoritukset.head.osasuoritukset.toList.flatten.collect { case s: IBDiplomanCoreSuoritus => s.koulutusmoduuli }

  "Diploma awarded" - {
    val oo = converter.convert(rivi("abc123"), kohde, vahvistuspäivä).toOption.get
    val suoritus = oo.suoritukset.head

    "saa tunnisteen, oppilaitoksen ja valmistunut-tilan vahvistuspäivälle" in {
      oo.iboTunniste should be("abc123")
      oo.oppilaitos.map(_.oid) should be(Some(ressunLukio.oid))
      oo.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)) should be(List((vahvistuspäivä, "valmistunut")))
      oo.alkamispäivä should be(Some(vahvistuspäivä))
      oo.päättymispäivä should be(Some(vahvistuspäivä))
    }

    "saa pisteet, tuloksen, tutkintokerran ja vahvistuksen" in {
      suoritus.kokonaispistemäärä should be(Some(37))
      suoritus.lisäpisteet should be(Some(2))
      suoritus.tulos.koodiarvo should be("D")
      suoritus.tutkintokerta should be(IBTutkintokerta("2016K", 2016, suoritus.tutkintokerta.vuodenaika))
      suoritus.tutkintokerta.vuodenaika.get("fi") should be("kevät")
      suoritus.vahvistus.map(_.päivä) should be(Some(vahvistuspäivä))
      suoritus.vahvistus.map(_.myöntäjäOrganisaatio).collect { case o: OrganisaatioWithOid => o.oid } should be(Some(ressunLukio.oid))
    }

    "muuntaa oppiaineet tasoineen, kielineen ja arvosanoineen, myös katkaistulla nimellä" in {
      val oppiaineet = suoritus.osasuoritukset.toList.flatten.collect { case s: IBDiplomanOppiaineenSuoritus => s }
      oppiaineet should have length 6
      val kieli = oppiaineet.head.koulutusmoduuli.asInstanceOf[IBDiplomanKieliOppiaine]
      kieli.tunniste.koodiarvo should be("A")
      kieli.kieli.koodiarvo should be("FI")
      kieli.taso.map(_.koodiarvo) should be(Some("HL"))
      oppiaineet.head.viimeisinArvosana should be(Some("7"))
      oppiaineet.head.arviointi.toList.flatten.head.päivä should be(Some(vahvistuspäivä))
      oppiaineet.map(_.koulutusmoduuli.tunniste.koodiarvo) should be(List("A", "B", "HIS", "BIO", "MATAA", "PHY"))
      oppiaineet.map(_.additionalSubject) should be(List.fill(6)(false))
    }

    "muuntaa extended essayn aineineen ja theory of knowledgen" in {
      val core = coreOppiaineet(oo)
      core.map(_.tunniste.koodiarvo) should be(List("EE", "TOK"))
      val aine = core.head.asInstanceOf[IBDiplomanExtendedEssay].aine.get
      aine.tunniste.koodiarvo should be("HIS")
      aine.taso should be(None)
      core(1) shouldBe an[IBDiplomanTheoryOfKnowledge]
      suoritus.osasuoritukset.toList.flatten.collect { case s: IBDiplomanCoreSuoritus => s.viimeisinArvosana } should be(List(Some("B"), Some("A")))
    }

    "täydentää koodistojen ja organisaatioiden nimet" in {
      suoritus.tulos.nimi should be(defined)
      oo.oppilaitos.flatMap(_.nimi) should be(defined)
    }
  }

  "Bilingual diploma, RETAKE" - {
    val oo = converter.convert(rivi("def456"), kohde, vahvistuspäivä).toOption.get
    val osasuoritukset = oo.suoritukset.head.osasuoritukset.toList.flatten

    "saa tuloskoodin B" in {
      oo.suoritukset.head.tulos.koodiarvo should be("B")
    }

    "säilyttää additional subjectin ja aiemman tutkintokerran, mutta jättää arvosanattoman oppiaineen pois" in {
      osasuoritukset should have length 1
      val oppiaine = osasuoritukset.head.asInstanceOf[IBDiplomanOppiaineenSuoritus]
      oppiaine.additionalSubject should be(true)
      oppiaine.tutkintokerta.koodiarvo should be("2015S")
      oppiaine.tutkintokerta.vuosi should be(2015)
    }
  }

  "Extended essay, jonka aineelle ei ole vastinetta Koskessa" - {
    "tuodaan ilman ainetta, ja siitä kirjataan varoitus" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map(s => if (s.level == "EE") s.copy(name = "WELSH A") else s))
      RootLogTester.clearMessages()
      val oo = converter.convert(muutettu, kohde, vahvistuspäivä).toOption.get
      val ee = coreOppiaineet(oo).head.asInstanceOf[IBDiplomanExtendedEssay]
      ee.tunniste.koodiarvo should be("EE")
      ee.aine should be(None)
      RootLogTester.getLogMessages.exists(_.contains("extended essayn aineelle WELSH A ei ole vastinetta Koskessa")) should be(true)
    }
  }

  "Ei tuotavat rivit" - {
    "CATEGORY COURSE hylätään" in {
      converter.convert(rivi("xyz999"), kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("xyz999", "CATEGORY-arvoa COURSE ei tuoda Koskeen")))
    }

    "failed diploma hylätään" in {
      val failedDiploma = rivi("abc123").copy(resultCode = IbisResultCode.FailedDiploma)
      converter.convert(failedDiploma, kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", "Tuloskoodia 'F' ei tuoda Koskeen")))
    }

    "oppiaine, jolle ei ole vastinetta Koskessa, hylkää koko rivin" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map(s => if (s.name == "BIOLOGY") s.copy(name = "ART HISTORY") else s))
      converter.convert(muutettu, kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", "IBO:n oppiaineelle 'ART HISTORY' ei ole vastinetta Koskessa")))
    }
  }

  "Kevään 2026 tulostiedosto" - {
    val tulokset = rivitTiedostosta("ib_diploma_kevat_2026_example.csv").map(converter.convert(_, kohde, LocalDate.of(2026, 7, 6)))
    val opiskeluoikeudet = tulokset.flatMap(_.toOption)

    "kaikki diplomat muunnetaan, myös IBIS:n katkaisemilla matematiikan nimillä" in {
      tulokset.collect { case Left(virhe) => virhe } should be(empty)
      val tunnisteet = opiskeluoikeudet.flatMap(_.suoritukset.head.osasuoritukset.toList.flatten).collect {
        case s: IBDiplomanOppiaineenSuoritus => s.koulutusmoduuli.tunniste.koodiarvo
      }
      tunnisteet should contain allOf("MATAA", "MATAI")
    }

    "kielioppiaineesta kirjoitetun extended essayn aine saa kielen" in {
      val ee = coreOppiaineet(opiskeluoikeudet(1)).collectFirst { case ee: IBDiplomanExtendedEssay => ee }.get
      val aine = ee.aine.get.asInstanceOf[IBDiplomanKieliOppiaine]
      aine.tunniste.koodiarvo should be("B")
      aine.kieli.koodiarvo should be("EN")
      aine.taso should be(None)
    }
  }
}
