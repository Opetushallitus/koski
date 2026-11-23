package fi.oph.koski.ibis

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
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
    KoskiApplicationForTests.validatingAndResolvingExtractor,
  )

  private val vahvistuspäivä = LocalDate.of(2016, 7, 6)

  private def rivi(iboTunniste: String): IbisRecord = rivit.find(_.iboTunniste == iboTunniste).get

  private def coreOppiaineet(oo: IBDiplomaOpiskeluoikeus): List[IBDiplomanCoreOppiaine] =
    oo.suoritukset.head.osasuoritukset.toList.flatten.collect { case s: IBDiplomanCoreSuoritus => s.koulutusmoduuli }

  "Diploma awarded" - {
    val oo = converter.convert(rivi("abc123"), None, vahvistuspäivä).toOption.get
    val suoritus = oo.suoritukset.head

    "saa tunnisteen, IBO:n oppilaitokseksi ja valmistunut-tilan vahvistuspäivälle" in {
      oo.oid should be(None)
      oo.iboTunniste should be("abc123")
      oo.oppilaitos.map(_.oid) should be(Some(IBO.oid))
      oo.koulutustoimija should be(None)
      oo.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)) should be(List((vahvistuspäivä, "valmistunut")))
      oo.alkamispäivä should be(Some(vahvistuspäivä))
      oo.päättymispäivä should be(Some(vahvistuspäivä))
    }

    "saa olemassa olevan diploman oidin, jotta tuonti päivittää sen" in {
      val diplomanOid = "1.2.246.562.15.12345678901"
      converter.convert(rivi("abc123"), Some(diplomanOid), vahvistuspäivä).toOption.flatMap(_.oid) should be(Some(diplomanOid))
    }

    "saa pisteet, tuloksen, tutkintokerran, IBO:n toimipisteeksi ja vahvistuksen" in {
      suoritus.totalPoints should be(Some(37))
      suoritus.combinedGrade should be(Some(2))
      suoritus.result.koodiarvo should be("D")
      suoritus.examinationSession should be(IBExaminationSession(year = 2016, month = 5))
      suoritus.vahvistus.map(_.päivä) should be(Some(vahvistuspäivä))
      suoritus.toimipiste.oid should be(IBO.oid)
      suoritus.vahvistus.map(_.myöntäjäOrganisaatio).collect { case o: OrganisaatioWithOid => o.oid } should be(Some(IBO.oid))
    }

    "merkitsee osasuorituksille tutkintokerran" in {
      val kerrat = suoritus.osasuoritukset.toList.flatten.map(_.examinationSession).distinct
      kerrat should be(List(IBExaminationSession(year = 2016, month = 5)))
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
      oppiaineet.map(_.suorituskieli.map(_.koodiarvo)) should be(Some("FI") :: List.fill(5)(Some("EN")))
    }

    "muuntaa extended essayn aineineen ja theory of knowledgen" in {
      val core = coreOppiaineet(oo)
      core.map(_.tunniste.koodiarvo) should be(List("EE", "TOK"))
      val aine = core.head.asInstanceOf[IBDiplomanExtendedEssay].aine.get
      aine.tunniste.koodiarvo should be("HIS")
      aine.taso should be(None)
      core(1) shouldBe an[IBDiplomanTheoryOfKnowledge]
      val coreSuoritukset = suoritus.osasuoritukset.toList.flatten.collect { case s: IBDiplomanCoreSuoritus => s }
      coreSuoritukset.map(_.viimeisinArvosana) should be(List(Some("B"), Some("A")))
      coreSuoritukset.map(_.suorituskieli.map(_.koodiarvo)) should be(List(Some("EN"), Some("EN")))
    }

    "täydentää koodistojen ja organisaatioiden nimet" in {
      suoritus.result.nimi should be(defined)
      suoritus.osasuoritukset.toList.flatten.head.suorituskieli.flatMap(_.nimi) should be(defined)
      oo.oppilaitos.flatMap(_.nimi) should be(defined)
    }
  }

  "Bilingual diploma, RETAKE" - {
    val oo = converter.convert(rivi("def456"), None, vahvistuspäivä).toOption.get
    val osasuoritukset = oo.suoritukset.head.osasuoritukset.toList.flatten

    "saa tuloskoodin B" in {
      oo.suoritukset.head.result.koodiarvo should be("B")
    }

    "säilyttää additional subjectin ja aiemman tutkintokerran, mutta jättää arvosanattoman oppiaineen pois" in {
      osasuoritukset should have length 1
      val oppiaine = osasuoritukset.head.asInstanceOf[IBDiplomanOppiaineenSuoritus]
      oppiaine.additionalSubject should be(true)
      oppiaine.examinationSession should be(IBExaminationSession(year = 2015, month = 11))
    }

    "kertoo pois jätetystä oppiaineesta varoituksella" in {
      converter.varoitukset(rivi("def456")) should be(List("Oppiaine HISTORY (HL) jätetään pois, koska arvosanan tilalla on N (no grade issued)"))
    }
  }

  "Ilman varoituksia muunnettavalla rivillä" - {
    "ei ole varoituksia" in {
      converter.varoitukset(rivi("abc123")) should be(empty)
    }
  }

  "Extended essay, jonka aineelle ei ole vastinetta Koskessa" - {
    "tuodaan ilman ainetta, ja siitä annetaan varoitus" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map(s => if (s.level == "EE") s.copy(name = "WELSH A") else s))
      val oo = converter.convert(muutettu, None, vahvistuspäivä).toOption.get
      val ee = coreOppiaineet(oo).head.asInstanceOf[IBDiplomanExtendedEssay]
      ee.tunniste.koodiarvo should be("EE")
      ee.aine should be(None)
      converter.varoitukset(muutettu) should be(List("Extended essayn aineelle WELSH A ei ole vastinetta Koskessa, joten aine jätetään pois"))
    }
  }

  "Oppiaine, jonka kielelle ei ole vastinetta Koskessa" - {
    "tuodaan ilman suorituskieltä, ja siitä annetaan varoitus" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map(s => if (s.name == "HISTORY" && s.level == "HL") s.copy(language = "BEMBA") else s))
      val oo = converter.convert(muutettu, None, vahvistuspäivä).toOption.get
      val historia = oo.suoritukset.head.osasuoritukset.toList.flatten.collectFirst { case s: IBDiplomanOppiaineenSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "HIS" => s }.get
      historia.suorituskieli should be(None)
      converter.varoitukset(muutettu) should be(List("Oppiaineen HISTORY (HL) kielelle BEMBA ei ole vastinetta Koskessa, joten suorituskieli jätetään pois"))
    }
  }

  "Ei tuotavat rivit" - {
    "CATEGORY COURSE hylätään" in {
      converter.convert(rivi("xyz999"), None, vahvistuspäivä) should be(Left(IbisKonversiovirhe("xyz999", List("CATEGORY-arvoa COURSE ei tuoda Koskeen"))))
    }

    "failed diploma hylätään" in {
      val failedDiploma = rivi("abc123").copy(resultCode = IbisResultCode.FailedDiploma)
      converter.convert(failedDiploma, None, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", List("Tuloskoodia 'F' ei tuoda Koskeen"))))
    }

    "oppiaine, jolle ei ole vastinetta Koskessa, hylkää koko rivin" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map(s => if (s.name == "BIOLOGY") s.copy(name = "ART HISTORY") else s))
      converter.convert(muutettu, None, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", List("IBO:n oppiaineelle 'ART HISTORY' ei ole vastinetta Koskessa"))))
    }

    "rivin kaikki virheet kerätään" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(subjects = alkuperäinen.subjects.map {
        case s if s.name == "BIOLOGY" => s.copy(name = "ART HISTORY", grade = "X")
        case s if s.name == "PHYSICS" => s.copy(name = "REF PROJECT")
        case s if s.level == "TK" => s.copy(grade = "X")
        case s => s
      })
      converter.convert(muutettu, None, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", List(
        "IBO:n oppiaineelle 'ART HISTORY' ei ole vastinetta Koskessa",
        "Koodiarvoa 'X' ei löydy koodistosta arviointiasteikkoib",
        "IBO:n oppiaineelle 'REF PROJECT' ei ole vastinetta Koskessa",
        "Koodiarvoa 'X' ei löydy koodistosta arviointiasteikkocorerequirementsib",
      ))))
    }

    "toistuva virhe kerrotaan vain kerran" in {
      val alkuperäinen = rivi("abc123")
      val muutettu = alkuperäinen.copy(month = "JUNE", subjects = alkuperäinen.subjects.map(_.copy(month = "JUNE")))
      converter.convert(muutettu, None, vahvistuspäivä) should be(Left(IbisKonversiovirhe("abc123", List("Tuntematon tutkintokerran kuukausi JUNE"))))
    }
  }

  "Validoinnin virheistä" - {
    "saadaan kaikki syyt" in {
      val status = HttpStatus.fold(KoskiErrorCategory.badRequest("Ensimmäinen virhe"), KoskiErrorCategory.badRequest("Toinen virhe"))
      IbisKonversiovirhe.syyt(status) should be(List("Ensimmäinen virhe", "Toinen virhe"))
    }
  }

  "Diploma, jossa on kielioppiaineita usealla kielellä" - {
    val oo = converter.convert(rivi("ghi789"), None, vahvistuspäivä).toOption.get
    val oppiaineet = oo.suoritukset.head.osasuoritukset.toList.flatten.collect { case s: IBDiplomanOppiaineenSuoritus => s }

    "saa kielioppiaineille kielen ja suorituskielen" in {
      oppiaineet.flatMap(s => s.koulutusmoduuli match {
        case k: IBDiplomanKieliOppiaine => Some((k.tunniste.koodiarvo, k.kieli.koodiarvo, s.suorituskieli.map(_.koodiarvo)))
        case _ => None
      }) should be(List(("A2", "EN", Some("EN")), ("B", "NO", Some("NO")), ("AB", "JA", Some("JA"))))
    }

    "säilyttää aiemmalla tutkintokerralla suoritetun oppiaineen tutkintokerran" in {
      oppiaineet.find(_.koulutusmoduuli.tunniste.koodiarvo == "B").map(_.examinationSession) should be(Some(IBExaminationSession(year = 2015, month = 5)))
    }

    "tunnistaa IBIS:n katkaiseman matematiikan nimen" in {
      oppiaineet.map(_.koulutusmoduuli.tunniste.koodiarvo) should contain("MATAI")
    }

    "kielioppiaineesta kirjoitetun extended essayn aine saa kielen" in {
      val ee = coreOppiaineet(oo).collectFirst { case ee: IBDiplomanExtendedEssay => ee }.get
      val aine = ee.aine.get.asInstanceOf[IBDiplomanKieliOppiaine]
      aine.tunniste.koodiarvo should be("B")
      aine.kieli.koodiarvo should be("EN")
      aine.taso should be(None)
    }
  }
}
