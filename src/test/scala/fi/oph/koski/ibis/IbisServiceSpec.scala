package fi.oph.koski.ibis

import fi.oph.koski.documentation.{ExamplesIBDiploma, ExamplesInternationalSchool}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{IBDiplomaOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, LukionOpiskeluoikeudenTila}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import scala.io.Source

class IbisServiceSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
  }

  private val oppija = "1.2.246.562.24.00000000001"
  private val toinenOppija = "1.2.246.562.24.00000000002"

  private val valmistunut = ExamplesIBDiploma.ibKoulutuksenOpiskeluoikeus
  private val keskeneräinen = valmistunut.copy(tila = LukionOpiskeluoikeudenTila(valmistunut.tila.opiskeluoikeusjaksot.take(1)))
  private val internationalSchool = ExamplesInternationalSchool.opiskeluoikeus
  private val diploma = ExamplesIBDiploma.opiskeluoikeus.copy(oid = Some("1.2.246.562.15.12345678901"))

  private def virhe(syy: String) = Left(IbisKonversiovirhe("abc123", List(syy)))
  private val usealtaOppijalta = virhe("IBO-tunnisteella löytyy usean oppijan opiskeluoikeuksia")

  "Yhdistämisen säännöt" - {
    def yhdistä(löydetyt: (String, KoskeenTallennettavaOpiskeluoikeus)*) =
      IbisService.yhdistäOppijaan("abc123", löydetyt.map { case (oppijaOid, oo) => IbisTunnisteenOpiskeluoikeus("abc123", oppijaOid, oo) })

    "valmistuneesta IB-koulutuksesta saadaan oppija" in {
      yhdistä(oppija -> valmistunut) should be(Right(IbisYhdistettyOppija(oppija, None)))
    }

    "keskeneräisestä IB-koulutuksesta saadaan oppija" in {
      yhdistä(oppija -> keskeneräinen) should be(Right(IbisYhdistettyOppija(oppija, None)))
    }

    "International schoolin opiskeluoikeudesta saadaan oppija" in {
      yhdistä(oppija -> internationalSchool) should be(Right(IbisYhdistettyOppija(oppija, None)))
    }

    "olemassa oleva diploma päivitetään" in {
      yhdistä(oppija -> valmistunut, oppija -> diploma) should be(Right(IbisYhdistettyOppija(oppija, diploma.oid)))
    }

    "diploma päivitetään, vaikka IB-koulutus ei ole valmistunut" in {
      yhdistä(oppija -> keskeneräinen, oppija -> diploma) should be(Right(IbisYhdistettyOppija(oppija, diploma.oid)))
    }

    "saman oppijan useasta valmistuneesta IB-koulutuksesta saadaan oppija" in {
      yhdistä(oppija -> valmistunut, oppija -> valmistunut) should be(Right(IbisYhdistettyOppija(oppija, None)))
    }

    "toisen oppijan keskeneräinen IB-koulutus estää yhdistämisen" in {
      yhdistä(oppija -> valmistunut, toinenOppija -> keskeneräinen) should be(usealtaOppijalta)
    }

    "toisen oppijan International schoolin opiskeluoikeus estää yhdistämisen" in {
      yhdistä(oppija -> valmistunut, toinenOppija -> internationalSchool) should be(usealtaOppijalta)
    }

    "ilman opiskeluoikeuksia ei yhdistetä" in {
      yhdistä() should be(virhe("Oppijaa ei voitu tunnistaa, koska IBO-tunnisteella ei löytynyt olemassa olevaa opiskeluoikeutta"))
    }

    "usean oppijan IB-koulutuksiin ei yhdistetä" in {
      yhdistä(oppija -> valmistunut, toinenOppija -> valmistunut) should be(usealtaOppijalta)
    }

    "IB-koulutukseen ei yhdistetä, jos diploma on toisella oppijalla" in {
      yhdistä(oppija -> valmistunut, toinenOppija -> diploma) should be(usealtaOppijalta)
    }

    "useaan diplomaan ei yhdistetä" in {
      val toinenDiploma = diploma.copy(oid = Some("1.2.246.562.15.10987654321"))
      yhdistä(oppija -> valmistunut, oppija -> diploma, oppija -> toinenDiploma) should be(
        virhe("IBO-tunnisteella löytyy useampi IB Diploma -opiskeluoikeus")
      )
    }
  }

  "Haku tietokannasta" - {
    lazy val service = KoskiApplicationForTests.ibisService

    "löytää oppijan valmistuneen IB-koulutuksen ja diploman tunnisteella kirjainkoosta riippumatta" in {
      service.yhdistäOppijoihin(List(rivi(ExamplesIBDiploma.iboTunniste.toUpperCase))).map(_._2.map(k => (k.oppijaOid, k.diplomanOid.isDefined))) should be(
        List(Right((KoskiSpecificMockOppijat.ibDiploma.oid, true)))
      )
    }

    "löytää oppijan International schoolin opiskeluoikeuden tunnisteella" in {
      service.yhdistäOppijoihin(List(rivi("ibo1"))).map(_._2) should be(
        List(Right(IbisYhdistettyOppija(KoskiSpecificMockOppijat.internationalschool.oid, None)))
      )
    }

    "hylkää tunnisteen, joka toistuu tiedostossa" in {
      val tunnisteet = List(ExamplesIBDiploma.iboTunniste, ExamplesIBDiploma.iboTunniste.toUpperCase)
      service.yhdistäOppijoihin(tunnisteet.map(rivi)).map(_._2) should be(
        tunnisteet.map(tunniste => Left(IbisKonversiovirhe(tunniste, List("Sama IBO-tunniste on tiedostossa usealla rivillä"))))
      )
    }
  }

  "Tuonti" - {
    lazy val service = KoskiApplicationForTests.ibisService
    implicit lazy val pääkäyttäjä: KoskiSpecificSession = MockUsers.paakayttaja.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    lazy val esimerkki = Source.fromInputStream(getClass.getResourceAsStream("/ibis/esimerkki.csv"), "UTF-8").mkString

    // Esimerkkitiedoston ensimmäinen rivi kiintoaineiston IB Diploma -oppijalle ja perään rivi, jota ei voi jäsentää.
    lazy val tiedosto = (esimerkki.replace("\"abc123\"", s"\"${ExamplesIBDiploma.iboTunniste}\"") + "rikkinäinen rivi\n")
      .getBytes(StandardCharsets.UTF_8)
    lazy val tiedostonTunnisteet = s"${ExamplesIBDiploma.iboTunniste},xyz999,def456,ghi789"

    def diplomanVersio: Option[Int] = KoskiApplicationForTests.possu
      .findByOppijaOids(List(KoskiSpecificMockOppijat.ibDiploma.oid))(KoskiSpecificSession.systemUser)
      .collectFirst { case diploma: IBDiplomaOpiskeluoikeus => diploma.versionumero }
      .flatten

    "esikatselu raportoi rivit tallentamatta" in {
      val versioEnnen = diplomanVersio
      AuditLogTester.clearMessages()
      val raportti = service.esikatsele(tiedosto)

      raportti.tallennettu should be(false)
      raportti.jäsennysvirheet.map(_.line) should be(List(5))
      raportti.rivit.map(rivi => (rivi.iboTunniste, rivi.tila)) should be(List(
        ExamplesIBDiploma.iboTunniste -> IbisTuonninRivinTila.Päivitys,
        "xyz999" -> IbisTuonninRivinTila.Hylätty,
        "def456" -> IbisTuonninRivinTila.Hylätty,
        "ghi789" -> IbisTuonninRivinTila.Hylätty,
      ))
      val päivitys = raportti.rivit.head
      (päivitys.oppijaOid, päivitys.virheet) should be((Some(KoskiSpecificMockOppijat.ibDiploma.oid), Nil))
      päivitys.diplomanOid should be(defined)
      diplomanVersio should be(versioEnnen)
      AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
        "operation" -> "IBIS_ESIKATSELU",
        "target" -> Map("iboTunniste" -> tiedostonTunnisteet),
      ))
    }

    "esikatselu jakaa suuren tiedoston tunnisteet useaan audit-lokiviestiin" in {
      val tunnisteet = (1 to 250).map(i => s"ibis$i").toList
      val ensimmäinenRivi = esimerkki.linesIterator.next()
      val suuriTiedosto = tunnisteet.map(t => ensimmäinenRivi.replace("\"abc123\"", s"\"$t\"") + "\n").mkString.getBytes(StandardCharsets.UTF_8)
      AuditLogTester.clearMessages()
      service.esikatsele(suuriTiedosto)

      val kirjatut = AuditLogTester.getLogMessages.map(JsonMethods.parse(_)).collect {
        case viesti if (viesti \ "operation") == JString("IBIS_ESIKATSELU") => (viesti \ "target" \ "iboTunniste").values.toString
      }
      kirjatut.map(_.split(",").length) should be(List(100, 100, 50))
      kirjatut.flatMap(_.split(",")) should be(tunnisteet)
    }

    "tuonti päivittää olemassa olevan diploman" in {
      val versioEnnen = diplomanVersio
      AuditLogTester.clearMessages()
      val raportti = service.tuo(tiedosto)

      raportti.tallennettu should be(true)
      raportti.rivit.map(rivi => (rivi.tila, rivi.virheet)).head should be((IbisTuonninRivinTila.Päivitys, Nil))
      diplomanVersio should be(versioEnnen.map(_ + 1))
      AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
        "operation" -> "IBIS_TALLENNUS",
        "target" -> Map("iboTunniste" -> tiedostonTunnisteet),
      ))
      AuditLogTester.verifyLastAuditLogMessageForOperation(Map(
        "operation" -> "OPISKELUOIKEUS_MUUTOS",
        "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.ibDiploma.oid),
      ))
    }
  }

  private def rivi(iboTunniste: String) = IbisRecord(
    year = 2016,
    month = "MAY",
    school = "000001",
    iboTunniste = iboTunniste,
    dateOfBirth = None,
    gender = "",
    category = IbisCategory.Diploma,
    lastName = "",
    firstName = "",
    studentId = "",
    courseRef = "",
    lastChangeDate = None,
    totalPoints = Some(37),
    resultCode = IbisResultCode.DiplomaAwarded,
    combinedGrade = Some(2),
    subjects = Nil,
  )
}
