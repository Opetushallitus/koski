package fi.oph.koski.kela

import java.time.LocalDate

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class KelaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with BeforeAndAfterAll {

  "Kelan yhden oppijan rajapinta" - {
    "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      postHetu(MockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.amis.oid)))
      }
    }
    "Palautetaan 404 jos opiskelijalla ei ole ollenkaan Kelaa kiinnostavia opiskeluoikeuksia" in {
      postHetu(MockOppijat.monimutkainenKorkeakoululainen.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Oppijan opiskeluoikeuksista filtteröidään pois sellaiset opiskeluoikeuden tyypit jotka ei kiinnosta Kelaa" in {
      postHetu(MockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[KelaOppija](body)

        response.henkilö.hetu should equal(MockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List(OpiskeluoikeudenTyyppi.perusopetus.koodiarvo))
      }
    }
  }

  "Usean oppijan rajapinta" - {
    "Voidaan hakea usea oppija, jos jollain oppijalla ei löydy Kosken kantaan tallennettuja opintoja, se puuttuu vastauksesta" in {
      val hetut = List(
        MockOppijat.amis,
        MockOppijat.ibFinal,
        MockOppijat.koululainen,
        MockOppijat.ylioppilas
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[KelaOppija]](body)
        response.map(_.henkilö.hetu.get).sorted should equal(hetut.sorted.filterNot(_ == MockOppijat.ylioppilas.hetu.get))
      }
    }
    "Luo AuditLogin" in {
      AuditLogTester.clearMessages
      postHetut(List(MockOppijat.amis.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.amis.oid)))
      }
    }
    "Ei luo AuditLogia jos hetulla löytyvä oppija puuttuu vastauksesta" in {
      AuditLogTester.clearMessages
      postHetut(List(MockOppijat.korkeakoululainen.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.getLogMessages.length should equal(0)
      }
    }
    "Sallitaan 1000 hetua" in {
      val hetut = List.fill(1000)(MockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatusOk()
      }
    }
    "Ei sallita yli 1000 hetua" in {
      val hetut = List.fill(1001)(MockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Liian monta hetua, enintään 1000 sallittu"))
      }
    }
  }

  "Kelan käyttöoikeudet" - {
    "Suppeilla Kelan käyttöoikeuksilla ei nää kaikkia lisätietoja" in {
      postHetu(MockOppijat.amis.hetu.get, user = MockUsers.kelaSuppeatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get

        lisatiedot.hojks should equal(None)
        lisatiedot.opiskeluvalmiuksiaTukevatOpinnot should equal(None)

        opiskeluoikeudet.length should be(1)
      }
    }
    "Laajoilla Kelan käyttöoikeuksilla näkee kaikki KelaSchema:n lisätiedot" in {
      postHetu(MockOppijat.amis.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get

        lisatiedot.hojks shouldBe(defined)
        lisatiedot.opiskeluvalmiuksiaTukevatOpinnot shouldBe(defined)

        opiskeluoikeudet.length should be(1)
      }
    }
  }

  "Perusopetuksen oppiaineen oppimäärän suorituksesta ei välitetä suoritustapaa Kelalle" in {
    postHetu(MockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa.hetu.get) {
      verifyResponseStatusOk()
      val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet

      opiskeluoikeudet.foreach(_.suoritukset.foreach(suoritus => {
        suoritus.suoritustapa should equal(None)
      }))
    }
  }

  "Opiskeluoikeuden versiohistorian haku tuottaa AuditLogin" in {
    resetFixtures
    val opiskeluoikeus = lastOpiskeluoikeusByHetu(MockOppijat.amis)

    luoVersiohistoriaanRivi(MockOppijat.amis, opiskeluoikeus.asInstanceOf[AmmatillinenOpiskeluoikeus])

    AuditLogTester.clearMessages

    getVersiohistoria(opiskeluoikeus.oid.get) {
      verifyResponseStatusOk()
      val history = JsonSerializer.parse[List[OpiskeluoikeusHistoryPatch]](body)

      history.length should equal(2)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN", "target" -> Map("opiskeluoikeusOid" -> opiskeluoikeus.oid.get)))
    }
  }

  "Tietyn version haku opiskeluoikeudesta tuottaa AuditLogin" in {
    resetFixtures

    val opiskeluoikeus = lastOpiskeluoikeusByHetu(MockOppijat.amis)

    luoVersiohistoriaanRivi(MockOppijat.amis, opiskeluoikeus.asInstanceOf[AmmatillinenOpiskeluoikeus])

    AuditLogTester.clearMessages

    getOpiskeluoikeudenVersio(MockOppijat.amis.oid, opiskeluoikeus.oid.get, 1) {
      verifyResponseStatusOk()
      val response = JsonSerializer.parse[KelaOppija](body)

      response.opiskeluoikeudet.headOption.flatMap(_.versionumero) should equal(Some(1))
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.amis.oid)))
    }
  }

  private def postHetu[A](hetu: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(KelaRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetut",
      JsonSerializer.writeWithRoot(KelaBulkRequest(hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def getVersiohistoria[A](opiskeluoikeudenOid: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$opiskeluoikeudenOid", user)(f)
  }

  private def getOpiskeluoikeudenVersio[A](
    oppijaOid: String,
    opiskeluoikeudenOid: String,
    versio: Int,
    user: MockUser = MockUsers.kelaLaajatOikeudet
  )(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$oppijaOid/$opiskeluoikeudenOid/$versio", user)(f)
  }

  private def luoVersiohistoriaanRivi(oppija: Henkilö, opiskeluoikeus: AmmatillinenOpiskeluoikeus): Unit = {
    createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
  }
}
