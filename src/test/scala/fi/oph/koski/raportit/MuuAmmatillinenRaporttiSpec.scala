package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillinenTutkintoSuoritus, kiinteistösihteerinMuuAmmatillinenKoulutus, puutarhuri}
import fi.oph.koski.documentation.MuunAmmatillisenKoulutuksenExample.muunAmmatillisenKoulutuksenSuoritus
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.stadinAmmattiopisto
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class MuuAmmatillinenRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with AmmatillinenRaporttiTestMethods {

  override def beforeAll: Unit = {
    resetFixtures
    insertMuuAmmatillisenSuorituksenOpiskeluoikeusPäivämäärillä(MockOppijat.amis, alkanut = LocalDate.of(2019, 1, 2), päättynyt = LocalDate.of(2019, 12, 31))
    insertMuuAmmatillisenSuorituksenOpiskeluoikeusPäivämäärillä(MockOppijat.lukiolainen, alkanut = LocalDate.of(2017, 1, 1), päättynyt = LocalDate.of(2020, 1, 1))
    insertSisällytettyOpiskeluoikeusSuorituksilla(MockOppijat.eero, innerSuoritukset = List(muunAmmatillisenKoulutuksenSuoritus, ammatillinenTutkintoSuoritus(puutarhuri)), outerSuoritukset = List(kiinteistösihteerinMuuAmmatillinenKoulutus()))
    loadRaportointikantaFixtures
  }

  lazy val raportti = {
    val raporttiBuilder = MuuAmmatillinenRaporttiBuilder(KoskiApplicationForTests.raportointiDatabase.db)
    val alku = Date.valueOf(LocalDate.of(2018, 1, 1))
    val loppu = Date.valueOf(LocalDate.of(2019, 1, 1))
    raporttiBuilder.build(stadinAmmattiopisto, alku, loppu).rows.map(_.asInstanceOf[MuuAmmatillinenRaporttiRow])
  }

  "Muu ammatillinen suoritustietoraportti" - {
    "Voidaan ladata ja tuottaa auditlogin" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/muuammatillinen", expectedRaporttiNimi = "muuammatillinen", expectedFileNamePrefix = "muu_ammatillinen_koski_raportti")
    }

    "Raportin hakuväli päivämäärä rajaus" - {
      "Opiskeluoikeus alkanut raportin hakuvälin jälkeen" in {
        raportti.filter(_.hetu.exists(MockOppijat.amis.hetu.contains)).length should equal(0)
      }
      "Opiskeluoikeus alkanut ennen hakuväliä ja päättynyt hakuvälin jälkeen" in {
        raportti.filter(_.hetu.exists(MockOppijat.lukiolainen.hetu.contains)).length should equal(1)
      }
    }

    "Raportin sisältö" - {
      lazy val marjonRivi = findSingle(raportti, MockOppijat.muuAmmatillinen)
      lazy val eeronRivit = findMultiple(raportti, MockOppijat.eero, expectedRowCount = 2)
      lazy val eeronOuterOpiskeluoikeus = eeronRivit.find(_.sisältyyOpiskeluoikeuteenOid.isEmpty).get
      lazy val eeronInnerOpiskeluoikeus = eeronRivit.find(_.sisältyyOpiskeluoikeuteenOid.isDefined).get

      "Perustiedot" in {
        marjonRivi.sisältyyOpiskeluoikeuteenOid should equal(None)
        marjonRivi.lähdejärjestelmäKoodiarvo should equal(None)
        marjonRivi.lähdejärjestelmäId should equal(None)
        marjonRivi.toimipisteOid should equal(Some(MockOrganisaatiot.lehtikuusentienToimipiste))
        marjonRivi.suorituksenNimi should equal("Kiinteistösihteerin koulutus ja tutkinto (KISI)")
        marjonRivi.opiskeluoikeudenAlkamispäivä should equal(LocalDate.of(2018, 1, 1))
        marjonRivi.opiskeluoikeudenViimeisinTila should equal("lasna")
        marjonRivi.yksilöity should equal(true)
        marjonRivi.oppijaOid should equal(MockOppijat.muuAmmatillinen.oid)
        marjonRivi.etunimet should equal("Marjo")
        marjonRivi.sukunimi should equal("Muu-Ammatillinen")
      }
      "Suoritettujen osasuoritusten lukumäärä" in {
        marjonRivi.suoritutettujenOsasuoritustenLkm should equal(6)
      }
      "Keskeneräisten osasuoritusten lukumäärä" in {
        marjonRivi.keskeneräistenOsasuoritustenLkm should equal(1)
      }
      "Kaikkien osasuoritusten yhteislaajuus" in {
        marjonRivi.kaikkienOsasuoritustenYhteislaajuus should equal(40.0)
      }
      "Kaikkien osasuoritusten laajuuden yksiköt" in {
        marjonRivi.kaikkienOsasuoritustenLaajuudenYksiköt should equal(Some("opintopistettä"))
      }
      "Suoritettujen yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
        marjonRivi.suoritettujenYhteistenTutkinnonOsienOsaalueidenLkm should equal(2)
      }
      "Suoritettujen tutkinnon osaa pienempien kokonaisuuksien lukumäärä" in {
        marjonRivi.suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm should equal(0)
      }
      "Suoritettujen muu ammatillisen koulutuksen osasuoritusten lukumäärä" in {
        marjonRivi.suoritettujenMuuAmmatillisenKoulutuksenOsasuoritustenLkm should equal(4)
      }
      "Sisällytetty opiskeluoikeus otetaan raportille" in {
        Some(eeronOuterOpiskeluoikeus.opiskeluoikeusOid) should equal(eeronInnerOpiskeluoikeus.sisältyyOpiskeluoikeuteenOid)
      }
      "Sisällytetyn opiskeluoikeuden suoritusten lukumäärä" in {
        eeronOuterOpiskeluoikeus.suoritutettujenOsasuoritustenLkm should equal(0)
        eeronInnerOpiskeluoikeus.suoritutettujenOsasuoritustenLkm should equal(6)
      }
    }
  }

  def findSingle(rows: Seq[MuuAmmatillinenRaporttiRow], oppija: LaajatOppijaHenkilöTiedot)= {
    val result = rows.filter(_.hetu.exists(oppija.hetu.contains))
    result.length should equal(1)
    result.head
  }

  def findMultiple(rows: Seq[MuuAmmatillinenRaporttiRow], oppija: LaajatOppijaHenkilöTiedot, expectedRowCount: Int) = {
    val result = rows.filter(_.hetu.exists(oppija.hetu.contains))
    result.length should equal(expectedRowCount)
    result
  }
}
