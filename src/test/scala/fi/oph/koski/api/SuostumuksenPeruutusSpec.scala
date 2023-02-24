package fi.oph.koski.api

import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.documentation.VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.koskiuser.MockUsers.{paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet, varsinaisSuomiPalvelukäyttäjä}
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation, RootLogTester}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{VapaanSivistystyönOpiskeluoikeus, OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso}
import org.json4s.{JObject, JString}
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.InetAddress.{getByName => inetAddress}
import java.time.LocalDate

class SuostumuksenPeruutusSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with SuoritusjakoTestMethods with SearchTestMethods with OpiskeluoikeudenMitätöintiJaPoistoTestMethods with BeforeAndAfterAll {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinen

  val vapaatavoitteinenHetu = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.hetu.get
  val vapaatavoitteinenOpiskeluoikeus = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head
  val vapaatavoitteinenOpiskeluoikeusMitätöity = vapaatavoitteinenOpiskeluoikeus match {
    case oo: VapaanSivistystyönOpiskeluoikeus =>
      val opiskeluoikeusjaksotMitätöity = oo.tila.opiskeluoikeusjaksot ++ Seq(OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(alku = oo.tila.opiskeluoikeusjaksot.head.alku, tila = opiskeluoikeusMitätöity))
      oo.copy(tila = oo.tila.copy(opiskeluoikeusjaksot = opiskeluoikeusjaksotMitätöity))
  }
  val vapaatavoitteinenOpiskeluoikeusOid = vapaatavoitteinenOpiskeluoikeus.oid.get

  val teijaHetu= KoskiSpecificMockOppijat.teija.hetu.get
  val teijaOpiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.teija.oid).head.oid.get

  override def beforeAll = resetFixtures

  "Kun suostumus voidaan peruuttaa" - {
    val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

    "Opiskeluoikeus on poistunut" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      authGet("api/opiskeluoikeus/" + vapaatavoitteinenOpiskeluoikeusOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Suostumuksen peruutuksesta jää audit-log -merkintä" in {
      resetFixtures()

      AuditLogTester.clearMessages
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(2)

      AuditLogTester.verifyAuditLogMessage(logMessages(1), Map(
        "operation" -> KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> vapaatavoitteinenOpiskeluoikeus.oid.get
        ),
      ))
    }

    "Suostumuksen peruutuksesta tehdään määrämuotoinen log-merkintä sähköpostinotifikaation lähetystä varten" in {
      resetFixtures()

      RootLogTester.clearMessages
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      RootLogTester.getLogMessages.find(_.startsWith("Kansalainen")).get should equal(s"Kansalainen perui suostumuksen. Opiskeluoikeus ${vapaatavoitteinenOpiskeluoikeusOid}. Ks. tarkemmat tiedot mock/koski/api/opiskeluoikeus/suostumuksenperuutus")
    }

    "Sähköpostinotifikaatiota varten voi tehdä testimerkinnän" in {
      RootLogTester.clearMessages
      get(s"/api/opiskeluoikeus/suostumuksenperuutus/testimerkinta", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatusOk()
      }

      RootLogTester.getLogMessages.find(_.startsWith("Kansalainen")).get should equal(s"Kansalainen perui suostumuksen. Opiskeluoikeus [TÄMÄ ON TESTIVIESTI]. Ks. tarkemmat tiedot mock/koski/api/opiskeluoikeus/suostumuksenperuutus")
    }

    "Testimerkintää ei voi tehdä ilman loginia" in {
      RootLogTester.clearMessages
      get(s"/api/opiskeluoikeus/suostumuksenperuutus/testimerkinta") {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated("Käyttäjä ei ole tunnistautunut."))
      }
    }

    "Testimerkintää ei voi tehdä kansalaisen tunnuksilla" in {
      RootLogTester.clearMessages
      get(s"/api/opiskeluoikeus/suostumuksenperuutus/testimerkinta", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu) ) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVirkailija("Sallittu vain virkailija-käyttäjille"))
      }
    }

    "Opiskeluoikeudesta jää raatorivi opiskeluoikeustauluun" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          vapaatavoitteinenOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(vapaatavoitteinenOpiskeluoikeusOid)
        ooRow.versionumero should be(vapaatavoitteinenOpiskeluoikeus.versionumero.get + 1)
        ooRow.aikaleima.toString should include(LocalDate.now.toString)
        ooRow.oppijaOid should be(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        ooRow.oppilaitosOid should be("")
        ooRow.koulutustoimijaOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOppilaitosOid should be(None)
        ooRow.data should be(JObject(List()))
        ooRow.luokka should be(None)
        ooRow.mitätöity should be(true)
        ooRow.koulutusmuoto should be("")
        ooRow.alkamispäivä.toString should be(LocalDate.now.toString)
        ooRow.päättymispäivä should be(None)
        ooRow.suoritusjakoTehty should be(false)
        ooRow.suoritustyypit should be(Nil)
        ooRow.poistettu should be(true)
      }
      )
    }

    "Opiskeluoikeus on poistunut OpenSearchista" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Suostumuksen perumisen jälkeen pääkäyttäjä näkee peruutetun suostumuksen" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()
        body should include (KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        body should include (LocalDate.now.toString)
        body should include (vapaatavoitteinenOpiskeluoikeusOid)
        body should include (vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.oid)
        body should include (vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Vain pääkäyttäjä voi nähdä peruutetut suostumukset" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      val loginHeaders = authHeaders(MockUsers.kalle)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVirkailija())
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      val oo = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      val oid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head.oid.get
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$oid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.suostumusPeruttu())
      }
    }

    "Koskeen ei voi päivittää oidin kautta samaa opiskeluoikeutta, joka on jo poistettu" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}

      val oo = defaultOpiskeluoikeus.copy(
        oid = Some(vapaatavoitteinenOpiskeluoikeusOid)
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${vapaatavoitteinenOpiskeluoikeusOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      resetFixtures()
      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession).get.length should equal (1)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession) should equal (None)
    }

    "Opiskeluoikeuden, josta on tehty suoritusjako, voi silti mitätöidä (vaikka kansalainen ei suostumusta voikaan peruuttaa)" in {
      resetFixtures
      val loginHeaders = kansalainenLoginHeaders(vapaatavoitteinenHetu)

      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus) {
        verifyResponseStatusOk()
      }

      val json =
        raw"""[{
        "oppilaitosOid": "${MockOrganisaatiot.varsinaisSuomenKansanopisto}",
        "suorituksenTyyppi": "vstvapaatavoitteinenkoulutus",
        "koulutusmoduulinTunniste": "099999"
      }]"""

      createSuoritusjako(json, vapaatavoitteinenHetu){
        verifyResponseStatusOk()
      }

      val oid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head.oid.get

      mitätöiOpiskeluoikeus(oid, MockUsers.paakayttaja)

      authGet("api/opiskeluoikeus/" + oid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }
  }

  "Kun suostumus voidaan peruuttaa ja opiskeluoikeus mitätöidään delete-routella" - {
    val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

    "Opiskeluoikeus on poistunut" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)
      authGet("api/opiskeluoikeus/" + vapaatavoitteinenOpiskeluoikeusOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Mitätöinnistä jää audit-log -merkintä" in {
      resetFixtures()

      AuditLogTester.clearMessages
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)

      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(2)

      val vapaatavoitteinenOpiskeluoikeusRow: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          vapaatavoitteinenOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      AuditLogTester.verifyAuditLogMessage(logMessages(1), Map(
        "operation" -> KoskiOperation.OPISKELUOIKEUS_MUUTOS.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid,
          KoskiAuditLogMessageField.opiskeluoikeusId.toString -> vapaatavoitteinenOpiskeluoikeusRow.map(_.id).getOrElse("").toString,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> (vapaatavoitteinenOpiskeluoikeus.versionumero.get + 1).toString
        ),
      ))
    }

    "Opiskeluoikeudesta jää raatorivi opiskeluoikeustauluun" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          vapaatavoitteinenOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(vapaatavoitteinenOpiskeluoikeusOid)
        ooRow.versionumero should be(vapaatavoitteinenOpiskeluoikeus.versionumero.get + 1)
        ooRow.aikaleima.toString should include(LocalDate.now.toString)
        ooRow.oppijaOid should be(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        ooRow.oppilaitosOid should be("")
        ooRow.koulutustoimijaOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOppilaitosOid should be(None)
        ooRow.data should be(JObject(List()))
        ooRow.luokka should be(None)
        ooRow.mitätöity should be(true)
        ooRow.koulutusmuoto should be("")
        ooRow.alkamispäivä.toString should be(LocalDate.now.toString)
        ooRow.päättymispäivä should be(None)
        ooRow.suoritusjakoTehty should be(false)
        ooRow.suoritustyypit should be(Nil)
        ooRow.poistettu should be(true)
      }
      )
    }

    "Opiskeluoikeus on poistunut OpenSearchista" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Mitätöinnin jälkeen pääkäyttäjä näkee mitätöinnin" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()

        val json = parse(body)
        val obj = json(0)

        (obj \\ "Opiskeluoikeuden oid") shouldBe JString(vapaatavoitteinenOpiskeluoikeusOid)
        (obj \\ "Oppijan oid") shouldBe JString(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        (obj \\ "Opiskeluoikeuden päättymispäivä") shouldBe JString(LocalDate.now.toString)
        (obj \\ "Mitätöity") shouldBe a[JString]
        (obj \\ "Suostumus peruttu") should not be a[JString]
        (obj \\ "Oppilaitoksen oid") shouldBe JString(vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.oid)
        (obj \\ "Oppilaitoksen nimi") shouldBe JString(vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)

      val oo = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      val oid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head.oid.get
      mitätöiOpiskeluoikeus(oid, MockUsers.paakayttaja)

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.suostumusPeruttu())
      }
    }

    "Koskeen ei voi päivittää oidin kautta samaa opiskeluoikeutta, joka on jo mitätöity" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)

      val oo = defaultOpiskeluoikeus.copy(
        oid = Some(vapaatavoitteinenOpiskeluoikeusOid)
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${vapaatavoitteinenOpiskeluoikeusOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      resetFixtures()
      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession).get.length should equal (1)

      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid, MockUsers.paakayttaja)

      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession) should equal (None)
    }
  }

  "Kun suostumus voidaan peruuttaa ja opiskeluoikeus mitätöidään put-routella" - {
    val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

    "Opiskeluoikeus on poistunut" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      authGet("api/opiskeluoikeus/" + vapaatavoitteinenOpiskeluoikeusOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Mitätöinnistä jää audit-log -merkintä" in {
      resetFixtures()

      AuditLogTester.clearMessages
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(1)

      val vapaatavoitteinenOpiskeluoikeusRow: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          vapaatavoitteinenOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      AuditLogTester.verifyAuditLogMessage(logMessages.head, Map(
        "operation" -> KoskiOperation.OPISKELUOIKEUS_MUUTOS.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid,
          KoskiAuditLogMessageField.opiskeluoikeusId.toString -> vapaatavoitteinenOpiskeluoikeusRow.map(_.id).getOrElse("").toString,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> (vapaatavoitteinenOpiskeluoikeus.versionumero.get + 1).toString
        ),
      ))
    }

    "Opiskeluoikeudesta jää raatorivi opiskeluoikeustauluun" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          vapaatavoitteinenOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(vapaatavoitteinenOpiskeluoikeusOid)
        ooRow.versionumero should be(vapaatavoitteinenOpiskeluoikeus.versionumero.get + 1)
        ooRow.aikaleima.toString should include(LocalDate.now.toString)
        ooRow.oppijaOid should be(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        ooRow.oppilaitosOid should be("")
        ooRow.koulutustoimijaOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOppilaitosOid should be(None)
        ooRow.data should be(JObject(List()))
        ooRow.luokka should be(None)
        ooRow.mitätöity should be(true)
        ooRow.koulutusmuoto should be("")
        ooRow.alkamispäivä.toString should be(LocalDate.now.toString)
        ooRow.päättymispäivä should be(None)
        ooRow.suoritusjakoTehty should be(false)
        ooRow.suoritustyypit should be(Nil)
        ooRow.poistettu should be(true)
      }
      )
    }

    "Opiskeluoikeus on poistunut OpenSearchista" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Mitätöinnin jälkeen pääkäyttäjä näkee mitätöinnin" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()

        val json = parse(body)
        val obj = json(0)

        (obj \\ "Opiskeluoikeuden oid") shouldBe JString(vapaatavoitteinenOpiskeluoikeusOid)
        (obj \\ "Oppijan oid") shouldBe JString(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        (obj \\ "Opiskeluoikeuden päättymispäivä") shouldBe JString(vapaatavoitteinenOpiskeluoikeusMitätöity.tila.opiskeluoikeusjaksot.last.alku.toString)
        (obj \\ "Mitätöity") shouldBe a[JString]
        (obj \\ "Suostumus peruttu") should not be a[JString]
        (obj \\ "Oppilaitoksen oid") shouldBe JString(vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.oid)
        (obj \\ "Oppilaitoksen nimi") shouldBe JString(vapaatavoitteinenOpiskeluoikeus.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeus, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      val oid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head.oid.get

      val ooMitätöity = oo match {
        case oo: VapaanSivistystyönOpiskeluoikeus =>
          val opiskeluoikeusjaksotMitätöity = oo.tila.opiskeluoikeusjaksot ++ Seq(OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(alku = oo.tila.opiskeluoikeusjaksot.head.alku, tila = opiskeluoikeusMitätöity))
          oo.copy(
            oid = Some(oid),
            tila = oo.tila.copy(opiskeluoikeusjaksot = opiskeluoikeusjaksotMitätöity)
          )
      }

      putOpiskeluoikeus(ooMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.suostumusPeruttu())
      }
    }

    "Koskeen ei voi päivittää oidin kautta samaa opiskeluoikeutta, joka on jo mitätöity" in {
      resetFixtures()
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        oid = Some(vapaatavoitteinenOpiskeluoikeusOid)
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${vapaatavoitteinenOpiskeluoikeusOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      resetFixtures()
      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession).get.length should equal (1)

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession) should equal (None)
    }
  }

  "Kun suostumusta ei voida peruuttaa" - {
    "Kansalainen ei voi peruuttaa kenenkään muun suostumusta" in {
      resetFixtures
      val loginHeaders = kansalainenLoginHeaders(teijaHetu)

      // API:n yli
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $vapaatavoitteinenOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
      // Kutsutaan suoraan serviceä
      val teijaSession = sessio(KoskiSpecificMockOppijat.teija.oid)
      KoskiApplicationForTests.suostumuksenPeruutusService.peruutaSuostumus(vapaatavoitteinenOpiskeluoikeusOid, None)(teijaSession).statusCode should equal (403)
    }
    "Vain vapaan sivistystyön vapaatavoitteisen suorituksen ja opiskeluoikeuden voi peruuttaa" in {
      resetFixtures

      val loginHeaders = kansalainenLoginHeaders(teijaHetu)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$teijaOpiskeluoikeusOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $teijaOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
    }

    "Kansalainen ei voi peruuttaa suostumusta, josta on tehty suoritusjako" in {
      resetFixtures
      val loginHeaders = kansalainenLoginHeaders(vapaatavoitteinenHetu)

      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus) {
        verifyResponseStatusOk()
      }

      val json =
        raw"""[{
          "oppilaitosOid": "${MockOrganisaatiot.varsinaisSuomenKansanopisto}",
          "suorituksenTyyppi": "vstvapaatavoitteinenkoulutus",
          "koulutusmoduulinTunniste": "099999"
        }]"""

      createSuoritusjako(json, vapaatavoitteinenHetu){
        verifyResponseStatusOk()
      }

      val oid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid).head.oid.get
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$oid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
    }

    "Kansalainen ei voi peruuttaa suostumusta, jos opiskeluoikeus on mitätöity" in {
      resetFixtures

      mitätöiOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusOid)

      val loginHeaders = kansalainenLoginHeaders(vapaatavoitteinenHetu)

      // API:n yli
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $vapaatavoitteinenOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
      // Kutsutaan suoraan serviceä
      val vapaatavoitteinenSession = sessio(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
      KoskiApplicationForTests.suostumuksenPeruutusService.peruutaSuostumus(vapaatavoitteinenOpiskeluoikeusOid, None)(vapaatavoitteinenSession).statusCode should equal (403)
    }

  }

  private def sessio(oid: String) = {
    new KoskiSpecificSession(
      AuthenticationUser(
        oid,
        "",
        "",
        None
      ),
      "",
      inetAddress("127.0.0.1"),
      "",
      Set.empty
    )
  }

  private def mitätöiOpiskeluoikeus(oid: String) = {
    delete(s"api/opiskeluoikeus/${oid}", headers = authHeaders(MockUsers.paakayttaja))(verifyResponseStatusOk())
  }

}
