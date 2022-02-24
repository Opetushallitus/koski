package fi.oph.koski.raportit

import fi.oph.koski.documentation.TutkinnonOsaaPienempiKokonaisuusExample
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.stadinAmmattiopisto
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class TOPKSAmmatillinenRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with AmmatillinenRaporttiTestMethods
    with DirtiesFixtures {

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override protected def alterFixture(): Unit = {
    insertTOPKSOpiskeluoikeusPäivämäärillä(KoskiSpecificMockOppijat.lukioKesken, alkanut = LocalDate.of(2017, 1, 2), päättynyt = LocalDate.of(2017, 12, 31))
    insertTOPKSOpiskeluoikeusPäivämäärillä(KoskiSpecificMockOppijat.amis, alkanut = LocalDate.of(2019, 1, 2), päättynyt = LocalDate.of(2019, 12, 31))
    insertTOPKSOpiskeluoikeusPäivämäärillä(KoskiSpecificMockOppijat.lukiolainen, alkanut = LocalDate.of(2017, 1, 1), päättynyt = LocalDate.of(2020, 1, 1))
    insertSisällytettyOpiskeluoikeusSuorituksilla(KoskiSpecificMockOppijat.eero, innerSuoritukset = List(TutkinnonOsaaPienempiKokonaisuusExample.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus), outerSuoritukset = List(TutkinnonOsaaPienempiKokonaisuusExample.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus.copy(osasuoritukset = None)))
    reloadRaportointikanta
  }

  lazy val raportti = {
    val raporttiBuilder = TOPKSAmmatillinenRaporttiBuilder(KoskiApplicationForTests.raportointiDatabase.db)
    val alku = LocalDate.of(2018, 1, 1)
    val loppu = LocalDate.of(2019, 1, 1)
    raporttiBuilder.build(stadinAmmattiopisto, alku, loppu, t).rows.map(_.asInstanceOf[TOPKSAmmatillinenRaporttiRow])
  }

  "Tutkinnon osaa pienemmistä kokonaisuuksista koostuvan suorituksen suoritustietoraportti (TOPKS)" - {

    "Voidaan ladata ja tuottaa auditlogin" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/topksammatillinen", expectedRaporttiNimi = "topksammatillinen", expectedFileNamePrefix = "topks_ammatillinen_koski_raportti")
    }

    "Raportin hakuvälin päivämäärä rajaus" - {
      "Opiskeluoikeus päättynyt ennen hakuväliä" in {
        raportti.filter(_.hetu.exists(KoskiSpecificMockOppijat.lukioKesken.hetu.contains)).length should equal(0)
      }
      "Opiskeluoikeus alkanut raportin hakuvälin jälkeen" in {
        raportti.filter(_.hetu.exists(KoskiSpecificMockOppijat.amis.hetu.contains)).length should equal(0)
      }
      "Opiskeluoikeus alkanut ennen hakuväliä ja päättynyt hakuvälin jälkeen" in {
        raportti.filter(_.hetu.exists(KoskiSpecificMockOppijat.lukiolainen.hetu.contains)).length should equal(1)
      }
    }

    "Kolumnien sisältö" - {
      lazy val pentinRivi = findSingle(raportti, KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus)
      lazy val eeronRivit = findMultiple(raportti, KoskiSpecificMockOppijat.eero, expectedRowCount = 2)
      lazy val eeronOuterOpiskeluoikeus = eeronRivit.find(_.sisältyyOpiskeluoikeuteenOid.isEmpty).get
      lazy val eeronInnerOpiskeluoikeus = eeronRivit.find(_.sisältyyOpiskeluoikeuteenOid.isDefined).get

      "Perustiedot" in {
        pentinRivi.sisältyyOpiskeluoikeuteenOid should equal(None)
        pentinRivi.lähdejärjestelmäKoodiarvo should equal(None)
        pentinRivi.lähdejärjestelmäId should equal(None)
        pentinRivi.toimipisteOid should equal(Some(MockOrganisaatiot.lehtikuusentienToimipiste))
        pentinRivi.suorituksenNimi should equal("Kiinteistösihteerin koulutus ja tutkinto (KISI)")
        pentinRivi.opiskeluoikeudenAlkamispäivä should equal(LocalDate.of(2018, 1, 1))
        pentinRivi.opiskeluoikeudenViimeisinTila should equal("lasna")
        pentinRivi.yksilöity should equal(true)
        pentinRivi.oppijaOid should equal(KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus.oid)
        pentinRivi.etunimet should equal("Pentti")
        pentinRivi.sukunimi should equal("Pieni-Kokonaisuus")
      }
      "Suoritettujen osasuoritusten lukumäärä" in {
        pentinRivi.suoritutettujenOsasuoritustenLkm should equal(4)
      }
      "Keskeneräisten osasuoritusten lukumäärä" in {
        pentinRivi.keskeneräistenOsasuoritustenLkm should equal(2)
      }
      "Kaikkien osasuoritusten yhteislaajuus" in {
        pentinRivi.kaikkienOsasuoritustenYhteislaajuus should equal(9.0)
      }
      "Kaikkien osasuoritusten laajuuden yksiköt" in {
        pentinRivi.kaikkienOsasuoritustenLaajuudenYksiköt should equal(Some("osaamispistettä"))
      }
      "Suoritettujen yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
        pentinRivi.suoritettujenYhteistenTutkinnonOsienOsaAlueidenLkm should equal(3)
      }
      "Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
        pentinRivi.suoritettujenYhteistenTutkinnonOsienOsaAlueidenYhteisLaajuus should equal(5.0)
      }
      "Tunnustettujen yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
        pentinRivi.tunnustettujenYhteistenTutkinnonOsienOsaAlueidenLkm should equal(2)
      }
      "Suoritettujen tutkinnon osaa pienempien kokonaisuuksien lukumäärä" in {
        pentinRivi.suoritettujenTutkinnonOsaaPienempienKokonaisuuksienLkm should equal(1)
      }
      "Sisällytetty opiskeluoikeus otetaan raportille" in {
        Some(eeronOuterOpiskeluoikeus.opiskeluoikeusOid) should equal(eeronInnerOpiskeluoikeus.sisältyyOpiskeluoikeuteenOid)
      }
      "Sisällytetyn opiskeluoikeuden suoritettujen osasuoristen lukumäärä" in {
        eeronOuterOpiskeluoikeus.suoritutettujenOsasuoritustenLkm should equal(0)
        eeronInnerOpiskeluoikeus.suoritutettujenOsasuoritustenLkm should equal(4)
      }
    }
  }

  def findSingle(rows: Seq[TOPKSAmmatillinenRaporttiRow], oppija: LaajatOppijaHenkilöTiedot)= {
    val result = rows.filter(_.hetu.exists(oppija.hetu.contains))
    result.length should equal(1)
    result.head
  }

  def findMultiple(rows: Seq[TOPKSAmmatillinenRaporttiRow], oppija: LaajatOppijaHenkilöTiedot, expectedRowCount: Int) = {
    val result = rows.filter(_.hetu.exists(oppija.hetu.contains))
    result.length should equal(expectedRowCount)
    result
  }
}
