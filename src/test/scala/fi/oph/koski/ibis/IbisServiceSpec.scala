package fi.oph.koski.ibis

import fi.oph.koski.documentation.ExamplesIBDiploma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, ressunLukio}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LukionOpiskeluoikeudenTila}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class IbisServiceSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
  }

  private val oppija = "1.2.246.562.24.00000000001"
  private val toinenOppija = "1.2.246.562.24.00000000002"

  private val valmistunut = ExamplesIBDiploma.ibKoulutuksenOpiskeluoikeus
  private val keskeneräinen = valmistunut.copy(tila = LukionOpiskeluoikeudenTila(valmistunut.tila.opiskeluoikeusjaksot.take(1)))
  private val aiemminValmistunut = valmistunut.copy(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = LukionOpiskeluoikeudenTila(valmistunut.tila.opiskeluoikeusjaksot.map(jakso => jakso.copy(alku = jakso.alku.minusYears(1)))),
  )
  private val diploma = ExamplesIBDiploma.opiskeluoikeus.copy(oid = Some("1.2.246.562.15.12345678901"), oppilaitos = Some(jyväskylänNormaalikoulu))

  private def virhe(syy: String) = Left(IbisKonversiovirhe("abc123", syy))
  private val usealtaOppijalta = virhe("Tunnisteella löytyy usean oppijan opiskeluoikeuksia")

  "Yhdistämisen säännöt" - {
    def yhdistä(löydetyt: (String, KoskeenTallennettavaOpiskeluoikeus)*) =
      IbisService.yhdistäOppijaan("abc123", löydetyt.map { case (oppijaOid, oo) => IbisTunnisteenOpiskeluoikeus("abc123", oppijaOid, oo) })

    "valmistuneesta IB-koulutuksesta saadaan oppija ja oppilaitos" in {
      yhdistä(oppija -> valmistunut) should be(Right(IbisYhdistettyOppija(oppija, ressunLukio, None)))
    }

    "olemassa oleva diploma päivitetään IB-koulutuksen oppilaitoksella" in {
      yhdistä(oppija -> valmistunut, oppija -> diploma) should be(Right(IbisYhdistettyOppija(oppija, ressunLukio, diploma.oid)))
    }

    "pelkkä diploma päivitetään sen nykyisellä oppilaitoksella" in {
      yhdistä(oppija -> keskeneräinen, oppija -> diploma) should be(Right(IbisYhdistettyOppija(oppija, jyväskylänNormaalikoulu, diploma.oid)))
    }

    "useasta valmistuneesta IB-koulutuksesta käytetään viimeksi päättyneen oppilaitosta" in {
      yhdistä(oppija -> aiemminValmistunut, oppija -> valmistunut).map(_.oppilaitos) should be(Right(ressunLukio))
    }

    "toisen oppijan keskeneräinen IB-koulutus ei estä yhdistämistä" in {
      yhdistä(oppija -> valmistunut, toinenOppija -> keskeneräinen).map(_.oppijaOid) should be(Right(oppija))
    }

    "keskeneräiseen IB-koulutukseen ei yhdistetä" in {
      yhdistä(oppija -> keskeneräinen) should be(virhe("Tunnisteella löytyy IB-koulutuksen opiskeluoikeus, mutta se ei ole valmistunut"))
    }

    "ilman opiskeluoikeuksia ei yhdistetä" in {
      yhdistä() should be(virhe("Tunnisteella ei löydy valmistunutta IB-koulutuksen opiskeluoikeutta eikä IB Diploma -opiskeluoikeutta"))
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
        virhe("Tunnisteella löytyy useampi IB Diploma -opiskeluoikeus")
      )
    }
  }

  "Haku tietokannasta" - {
    lazy val service = KoskiApplicationForTests.ibisService

    "löytää oppijan valmistuneen IB-koulutuksen ja diploman tunnisteella kirjainkoosta riippumatta" in {
      service.yhdistäOppijoihin(List(rivi(ExamplesIBDiploma.iboTunniste.toUpperCase))).map(_._2.map(k => (k.oppijaOid, k.oppilaitos.oid, k.diplomanOid.isDefined))) should be(
        List(Right((KoskiSpecificMockOppijat.ibDiploma.oid, ressunLukio.oid, true)))
      )
    }

    "ei yhdistä International schoolin opiskeluoikeuteen" in {
      service.yhdistäOppijoihin(List(rivi("ibo1"))).map(_._2) should be(
        List(Left(IbisKonversiovirhe("ibo1", "Tunnisteella ei löydy valmistunutta IB-koulutuksen opiskeluoikeutta eikä IB Diploma -opiskeluoikeutta")))
      )
    }

    "hylkää tunnisteen, joka toistuu tiedostossa" in {
      val tunnisteet = List(ExamplesIBDiploma.iboTunniste, ExamplesIBDiploma.iboTunniste.toUpperCase)
      service.yhdistäOppijoihin(tunnisteet.map(rivi)).map(_._2) should be(
        tunnisteet.map(tunniste => Left(IbisKonversiovirhe(tunniste, "Sama IBO-tunniste on tiedostossa usealla rivillä")))
      )
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
