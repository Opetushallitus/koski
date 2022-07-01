package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{BeforeAndAfterEach, Tag}
import java.time.LocalDate.{of => date}
import java.time.LocalDateTime

class ValpasRootApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    super.beforeEach()
    AuditLogTester.clearMessages
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    super.afterEach()
  }

  "Oppijan lataaminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPIJA_KATSOMINEN.toString,
        "target" -> Map(ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> oppijaOid)))
    }
  }

  "Oppilaitoksen oppijalistan hakeminen tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    authGet(getOppijaListUrl(oppilaitosOid)) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN.toString,
        "target" -> Map(ValpasAuditLogMessageField.juuriOrganisaatio.toString -> oppilaitosOid)))
    }
  }

  "Oppilaitoksen oppijalistan hakeminen hakutiedoilla tuottaa rivin auditlogiin" taggedAs(ValpasBackendTag) in {
    val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    val oppijaOids = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
    post(
      getOppijaListHakutiedoillaUrl(oppilaitosOid),
      JsonSerializer.writeWithRoot(Oppijalista(oppijaOids)),
      headers = authHeaders(defaultUser) ++ jsonContent,
    ) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN_HAKUTIEDOILLA.toString,
        "target" -> Map(
          ValpasAuditLogMessageField.juuriOrganisaatio.toString -> oppilaitosOid,
          ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> oppijaOids.mkString(","),
          ValpasAuditLogMessageField.sivu.toString -> "1",
          ValpasAuditLogMessageField.sivuLukumäärä.toString -> "1",
        )))
    }
  }

  "Ei-oppivelvollisen oppijan tietojen lataaminen ei onnistu" taggedAs(ValpasBackendTag) in {
    val oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.oid
    authGet(getOppijaUrl(oppijaOid)) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      AuditLogTester.verifyNoAuditLogMessages()
    }
  }

  "Oppija, johon ei ole oikeuksia, ja jota ei ole olemassa tuottavat saman vastaukset" taggedAs(ValpasBackendTag) in {
    val jklOppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021
    authGet(getOppijaUrl(jklOppija.oid), ValpasMockUsers.valpasHelsinkiPeruskoulu) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
      val firstResponse = response

      authGet(getOppijaUrl("1.2.3.4.5.6.7"), ValpasMockUsers.valpasHelsinkiPeruskoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija())
        response.body should equal (firstResponse.body)
        withoutVariatingEntries(response.headers) should equal (withoutVariatingEntries(firstResponse.headers))
        AuditLogTester.verifyNoAuditLogMessages()
      }
    }
  }


  // Hetuhauille on suurelta osin kattavat testit Valpas-fronttitesteissä, siksi tässä on vain osa tapauksista
  "Kunnan hetuhaku" - {
    "Hetu ei päädy lokiin - kunta" in {
      testHetunMaskausAccessLogissa(getHenkilöhakuKuntaUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
    }

    "Ei palauta vain oppijanumerorekisteristä löytyvää oppijaa käyttäjälle, jolla ei ole kunta-oikeuksia" in {
      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto("Käyttäjällä ei ole oikeuksia toimintoon"))
      }
    }

    "Palauttaa oppijan, joka löytyy vain oppijanumerorekisteristä" in {
      val expectedResult = ValpasLöytyiHenkilöhakuResult(
        oid = ValpasMockOppijat.eiKoskessaOppivelvollinen.oid,
        hetu = ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu,
        etunimet = ValpasMockOppijat.eiKoskessaOppivelvollinen.etunimet,
        sukunimi = ValpasMockOppijat.eiKoskessaOppivelvollinen.sukunimi,
        vainOppijanumerorekisterissä = true,
        maksuttomuusVoimassaAstiIänPerusteella = Some(date(2025, 12, 31))
      )

      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()

        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }

    "Ei palauta vain oppijanumerorekisterista löytyvää hetutonta oppijaa" in {
      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaHetuton.oid), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
      }
    }

    "ei palauta vain oppijanumerorekisteristä löytyvää oppijaa hänen täytettyään 18 vuotta" in {
      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2023, 1, 25))

      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
      }
    }
    "ei palauta Ahvenanmaalla asuvaa vain oppijanumerorekisteristä löytyvää oppijaa" in {
      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
      }
    }
    "palauttaa turvakiellollisen vain oppijanumerorekisteristä löytyvän oppijan" in {
      val expectedResult = ValpasLöytyiHenkilöhakuResult(
        oid = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.oid,
        hetu = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.hetu,
        etunimet = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.etunimet,
        sukunimi = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.sukunimi,
        vainOppijanumerorekisterissä = true,
        maksuttomuusVoimassaAstiIänPerusteella = Some(date(2025, 12, 31))
      )

      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()

        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }
    "ei palauta alle 18-vuotiasta ennen lain voimaantuloa syntynyttä vain oppijanumerorekisteristä löytyvää oppijaa" in {
      authGet(getHenkilöhakuKuntaUrl(ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
      }
    }
  }

  "Maksuttomuuskäyttäjän hetuhaku" - {
    "Hetu ei päädy lokiin - maksuttomuus" in {
      testHetunMaskausAccessLogissa(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
    }

    "Ei palauta vain oppijanumerorekisteristä löytyvää oppijaa käyttäjälle, jolla ei ole maksuttomuus-oikeuksia" in {
      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto("Käyttäjällä ei ole oikeuksia toimintoon"))
      }
    }

    "Palauttaa oppijan, joka löytyy vain oppijanumerorekisteristä" in {
      val expectedResult = ValpasLöytyiHenkilöhakuResult(
        oid = ValpasMockOppijat.eiKoskessaOppivelvollinen.oid,
        hetu = ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu,
        etunimet = ValpasMockOppijat.eiKoskessaOppivelvollinen.etunimet,
        sukunimi = ValpasMockOppijat.eiKoskessaOppivelvollinen.sukunimi,
        vainOppijanumerorekisterissä = true,
        maksuttomuusVoimassaAstiIänPerusteella = Some(date(2025, 12, 31))
      )

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()

        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }

    "Ei palauta vain oppijanumerorekisterista löytyvää hetutonta oppijaa" in {
      val expectedResult = ValpasEiLöytynytHenkilöhakuResult()

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaHetuton.oid), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }

    "ei palauta vain oppijanumerorekisteristä löytyvää oppijaa, kun on ohitettu 20-vuotisikävuosi" in {
      val expectedResult = ValpasEiLöytynytHenkilöhakuResult()

      KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(date(2026, 1, 1))

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }
    "ei palauta Ahvenanmaalla asuvaa vain oppijanumerorekisteristä löytyvää oppijaa" in {
      val expectedResult = ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
        None,
        None,
      )

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }
    "palauttaa turvakiellollisen vain oppijanumerorekisteristä löytyvän oppijan" in {
      val expectedResult = ValpasLöytyiHenkilöhakuResult(
        oid = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.oid,
        hetu = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.hetu,
        etunimet = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.etunimet,
        sukunimi = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.sukunimi,
        vainOppijanumerorekisterissä = true,
        maksuttomuusVoimassaAstiIänPerusteella = Some(date(2025, 12, 31))
      )

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()

        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }
    "ei palauta ennen lain voimaantuloa syntynyttä vain oppijanumerorekisteristä löytyvää oppijaa" in {
      val expectedResult = ValpasEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
        None,
        None,
      )

      authGet(getHenkilöhakuMaksuttomuusUrl(ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.hetu.get), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatusOk()
        val result = JsonSerializer.parse[ValpasHenkilöhakuResult](response.body)

        result should be(expectedResult)
      }
    }
  }

  "Suorittamiskäyttäjän hetuhaku" - {
    "Hetu ei päädy lokiin - suorittaminen" in {
      testHetunMaskausAccessLogissa(getHenkilöhakuSuorittaminenUrl(ValpasMockOppijat.lukionAloittanut.hetu.get))
    }

    "Ei palauta oppijaa, joka löytyy vain oppijanumerorekisteristä" in {
      authGet(getHenkilöhakuSuorittaminenUrl(ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get), ValpasMockUsers.valpasMonta) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.oppija("Käyttäjällä ei ole oikeuksia annetun oppijan tietoihin"))
      }
    }
  }

  private def testHetunMaskausAccessLogissa(url: String) = {
    AccessLogTester.clearMessages
    val maskedHetu = "******-****"
    authGet(url, ValpasMockUsers.valpasMonta) {
      verifyResponseStatusOk()
      Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
      AccessLogTester.getLogMessages.lastOption.get should include(maskedHetu)
    }
  }

  private def getHenkilöhakuKuntaUrl(hetu: String) = s"/valpas/api/henkilohaku/kunta/$hetu"
  private def getHenkilöhakuMaksuttomuusUrl(hetu: String) = s"/valpas/api/henkilohaku/maksuttomuus/$hetu"
  private def getHenkilöhakuSuorittaminenUrl(hetu: String) = s"/valpas/api/henkilohaku/suorittaminen/$hetu"

  def getOppijaUrl(oppijaOid: String) = s"/valpas/api/oppija/$oppijaOid"

  def getOppijaListUrl(organisaatioOid: String) = s"/valpas/api/oppijat/$organisaatioOid"
  def getOppijaListHakutiedoillaUrl(organisaatioOid: String) = s"/valpas/api/oppijat/$organisaatioOid/hakutiedot"

  def withoutVariatingEntries[T](headers: Map[String, T]) =
    headers.filterKeys(_ != "Date")
}

object ValpasBackendTag extends Tag("valpasback")

