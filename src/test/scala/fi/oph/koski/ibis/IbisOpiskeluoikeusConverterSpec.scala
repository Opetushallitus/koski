package fi.oph.koski.ibis

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.ressunLukio
import fi.oph.koski.schema._
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.io.Source

class IbisOpiskeluoikeusConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val rivit = IbisCsvParser.parse(Source.fromInputStream(getClass.getResourceAsStream("/ibis/esimerkki.csv"), "UTF-8").mkString)._2

  // TODO TOR-747: Oppiainekoodit ovat keksittyjä, kunnes IBO:n SUB cross-reference file on käytettävissä.
  private val oppiainekartta = IbisOppiainekartta(Map(
    "100001" -> IbisKieliOppiaine("A", "FI"),
    "100002" -> IbisKieliOppiaine("B", "EN"),
    "100003" -> IbisMuuOppiaine("HIS"),
    "100004" -> IbisMuuOppiaine("BIO"),
    "100005" -> IbisMuuOppiaine("MATAA"),
    "100006" -> IbisMuuOppiaine("PHY"),
    "100007" -> IbisMuuOppiaine("HIS"),
  ))

  private val converter = new IbisOpiskeluoikeusConverter(
    KoskiApplicationForTests.koodistoViitePalvelu,
    KoskiApplicationForTests.koskiLocalizationRepository,
    KoskiApplicationForTests.validatingAndResolvingExtractor,
    oppiainekartta,
  )

  private val kohde = IbisKohde("1.2.246.562.24.00000000001", ressunLukio, None)
  private val vahvistuspäivä = LocalDate.of(2016, 7, 6)

  private def rivi(personalCode: String): IbisRecord = rivit.find(_.personalCode == personalCode).get

  "Diploma awarded" - {
    val oo = converter.convert(rivi("ABC123"), kohde, vahvistuspäivä).toOption.get
    val suoritus = oo.suoritukset.head

    "saa tunnisteen, oppilaitoksen ja valmistunut-tilan vahvistuspäivälle" in {
      oo.iboTunniste should be("ABC123")
      oo.oppilaitos.map(_.oid) should be(Some(ressunLukio.oid))
      oo.tila.opiskeluoikeusjaksot.map(j => (j.alku, j.tila.koodiarvo)) should be(List((vahvistuspäivä, "valmistunut")))
      oo.alkamispäivä should be(Some(vahvistuspäivä))
      oo.päättymispäivä should be(Some(vahvistuspäivä))
    }

    "saa pisteet, tuloksen, tutkintokerran ja vahvistuksen" in {
      suoritus.kokonaispistemäärä should be(Some(37))
      suoritus.lisäpisteet should be(Some(2))
      suoritus.tulos.koodiarvo should be("D")
      suoritus.tutkintokerta should be(IBTutkintokerta("2016M", 2016, suoritus.tutkintokerta.kuukausi))
      suoritus.tutkintokerta.kuukausi.get("fi") should be("toukokuu")
      suoritus.vahvistus.map(_.päivä) should be(Some(vahvistuspäivä))
      suoritus.vahvistus.map(_.myöntäjäOrganisaatio).collect { case o: OrganisaatioWithOid => o.oid } should be(Some(ressunLukio.oid))
    }

    "muuntaa oppiaineet tasoineen, kielineen ja arvosanoineen" in {
      val oppiaineet = suoritus.osasuoritukset.toList.flatten.collect { case s: IBDiplomanOppiaineenSuoritus => s }
      oppiaineet should have length 6
      val kieli = oppiaineet.head.koulutusmoduuli.asInstanceOf[IBDiplomanKieliOppiaine]
      kieli.tunniste.koodiarvo should be("A")
      kieli.kieli.koodiarvo should be("FI")
      kieli.taso.koodiarvo should be("HL")
      kieli.iboOppiainekoodi should be(Some("100001"))
      oppiaineet.head.viimeisinArvosana should be(Some("7"))
      oppiaineet.head.arviointi.toList.flatten.head.päivä should be(Some(vahvistuspäivä))
      oppiaineet.map(_.koulutusmoduuli.tunniste.koodiarvo) should be(List("A", "B", "HIS", "BIO", "MATAA", "PHY"))
      oppiaineet.map(_.additionalSubject) should be(List.fill(6)(false))
    }

    "muuntaa extended essayn aineineen ja theory of knowledgen" in {
      val core = suoritus.osasuoritukset.toList.flatten.collect { case s: IBDiplomanCoreSuoritus => s }
      core.map(_.koulutusmoduuli.tunniste.koodiarvo) should be(List("EE", "TOK"))
      core.head.koulutusmoduuli.aine.map(_.koodiarvo) should be(Some("HIS"))
      core.map(_.viimeisinArvosana) should be(List(Some("B"), Some("A")))
    }

    "täydentää koodistojen ja organisaatioiden nimet" in {
      suoritus.tulos.nimi should be(defined)
      oo.oppilaitos.flatMap(_.nimi) should be(defined)
    }
  }

  "Bilingual diploma, RETAKE" - {
    val oo = converter.convert(rivi("DEF456"), kohde, vahvistuspäivä).toOption.get
    val osasuoritukset = oo.suoritukset.head.osasuoritukset.toList.flatten

    "saa tuloskoodin B" in {
      oo.suoritukset.head.tulos.koodiarvo should be("B")
    }

    "säilyttää additional subjectin ja aiemman tutkintokerran, mutta jättää arvosanattoman oppiaineen pois" in {
      osasuoritukset should have length 1
      val oppiaine = osasuoritukset.head.asInstanceOf[IBDiplomanOppiaineenSuoritus]
      oppiaine.additionalSubject should be(true)
      oppiaine.tutkintokerta.koodiarvo should be("2015N")
      oppiaine.tutkintokerta.vuosi should be(2015)
    }
  }

  "Ei tuotavat rivit" - {
    "CATEGORY COURSE hylätään" in {
      converter.convert(rivi("XYZ999"), kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("XYZ999", "CATEGORY-arvoa COURSE ei tuoda Koskeen")))
    }

    "failed diploma hylätään" in {
      val failedDiploma = rivi("ABC123").copy(resultCode = IbisResultCode.FailedDiploma)
      converter.convert(failedDiploma, kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("ABC123", "Tuloskoodia 'F' ei tuoda Koskeen")))
    }

    "tuntematon oppiainekoodi hylkää koko rivin" in {
      val tuntematon = rivi("ABC123")
      val muutettu = tuntematon.copy(subjects = tuntematon.subjects.map(s => if (s.code == "100003") s.copy(code = "999999") else s))
      converter.convert(muutettu, kohde, vahvistuspäivä) should be(Left(IbisKonversiovirhe("ABC123", "Tuntematon IBO:n oppiainekoodi 999999")))
    }
  }
}
