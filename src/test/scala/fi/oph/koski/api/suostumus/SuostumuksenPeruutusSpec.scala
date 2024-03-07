package fi.oph.koski.api.suostumus

import fi.oph.koski.api.misc._
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.documentation.VapaaSivistystyöExample
import fi.oph.koski.documentation.VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers.{paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet, varsinaisSuomiPalvelukäyttäjä}
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation, RootLogTester}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso, VapaanSivistystyönOpiskeluoikeus}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{JObject, JString}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.InetAddress.{getByName => inetAddress}
import java.time.LocalDate

class SuostumuksenPeruutusSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with SuoritusjakoTestMethods with SearchTestMethods with OpiskeluoikeudenMitätöintiJaPoistoTestMethods with BeforeAndAfterAll {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinen

  private val vapaatavoitteinenHetu = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.hetu.get

  private val teijaHetu= KoskiSpecificMockOppijat.teija.hetu.get

  private def teijaOpiskeluoikeusOid: String = {
    val oot = getOpiskeluoikeudet(KoskiSpecificMockOppijat.teija.oid)
    if (oot.length == 1) {
      oot.head.oid.get
    } else {
      throw new Exception("Oppijalta ei löytynyt yksiselitteistä opiskeluoikeutta")
    }
  }

  "Kun suostumus voidaan peruuttaa" - {
    "Opiskeluoikeus on poistunut" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }
      authGet("api/opiskeluoikeus/" + ooOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Suostumuksen peruutuksesta jää audit-log -merkintä" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      AuditLogTester.clearMessages

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }
      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(2)

      AuditLogTester.verifyAuditLogString(logMessages(1), Map(
        "operation" -> KoskiOperation.KANSALAINEN_SUOSTUMUS_PERUMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> ooOid
        ),
      ))
    }

    "Suostumuksen peruutuksesta tehdään määrämuotoinen log-merkintä sähköpostinotifikaation lähetystä varten" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      RootLogTester.clearMessages
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }

      RootLogTester.getLogMessages.find(_.startsWith("Kansalainen")).get should equal(s"Kansalainen perui suostumuksen. Opiskeluoikeus ${ooOid}. Ks. tarkemmat tiedot mock/koski/api/opiskeluoikeus/suostumuksenperuutus")
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
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          ooOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(ooOid)
        ooRow.versionumero should be(2)
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
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      KoskiApplicationForTests.perustiedotIndexer.sync(true)

      val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Suostumuksen perumisen jälkeen pääkäyttäjä näkee peruutetun suostumuksen" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()
        body should include (KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        body should include (LocalDate.now.toString)
        body should include (ooOid)
        body should include (oo.oppilaitos.get.oid)
        body should include (oo.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Vain pääkäyttäjä voi nähdä peruutetut suostumukset" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatus(200, Nil)
      }

      val loginHeaders = authHeaders(MockUsers.kalle)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVirkailija())
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista" in {
      val oo: VapaanSivistystyönOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus,
        headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent
      ).oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.suostumusPeruttu())
      }
    }

    "Koskeen ei voi päivittää oidin kautta samaa opiskeluoikeutta, joka on jo poistettu" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        oid = Some(ooOid)
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${ooOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession).get.length should equal (1)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {
        verifyResponseStatusOk()
      }
      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession) should equal (None)
    }

    "Opiskeluoikeuden, josta on tehty suoritusjako, voi silti mitätöidä (vaikka kansalainen ei suostumusta voikaan peruuttaa)" in {
      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus).oid.get

      val json =
        raw"""[{
        "oppilaitosOid": "${MockOrganisaatiot.varsinaisSuomenKansanopisto}",
        "suorituksenTyyppi": "vstvapaatavoitteinenkoulutus",
        "koulutusmoduulinTunniste": "099999"
      }]"""

      createSuoritusjako(json, vapaatavoitteinenHetu){
        verifyResponseStatusOk()
      }

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)

      authGet("api/opiskeluoikeus/" + ooOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }
  }

  "Kun suostumus voidaan peruuttaa ja opiskeluoikeus mitätöidään delete-routella" - {
    "Opiskeluoikeus on poistunut" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)
      authGet("api/opiskeluoikeus/" + ooOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Mitätöinnistä jää audit-log -merkintä" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      AuditLogTester.clearMessages
      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)

      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(2)

      val vapaatavoitteinenOpiskeluoikeusRow: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          ooOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      AuditLogTester.verifyAuditLogString(logMessages(1), Map(
        "operation" -> KoskiOperation.OPISKELUOIKEUS_MUUTOS.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid,
          KoskiAuditLogMessageField.opiskeluoikeusId.toString -> vapaatavoitteinenOpiskeluoikeusRow.map(_.id).getOrElse("").toString,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> (2).toString
        ),
      ))
    }

    "Opiskeluoikeudesta jää raatorivi opiskeluoikeustauluun" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          ooOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(ooOid)
        ooRow.versionumero should be(2)
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
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Mitätöinnin jälkeen pääkäyttäjä näkee mitätöinnin suostumuksen peruutus -routella" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid: String = oo.oid.get

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()

        val json = parse(body)
        val obj = json(0)

        (obj \\ "Opiskeluoikeuden oid") shouldBe JString(ooOid)
        (obj \\ "Oppijan oid") shouldBe JString(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        (obj \\ "Opiskeluoikeuden päättymispäivä") shouldBe JString(LocalDate.now.toString)
        (obj \\ "Mitätöity") shouldBe a[JString]
        (obj \\ "Suostumus peruttu") should not be a[JString]
        (obj \\ "Oppilaitoksen oid") shouldBe JString(oo.oppilaitos.get.oid)
        (obj \\ "Oppilaitoksen nimi") shouldBe JString(oo.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista, koska se on mitätöity" in {
      val ooLähdejärjestelmänIdllä: VapaanSivistystyönOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      val kirjoitettuOo = setupOppijaWithAndGetOpiskeluoikeus(ooLähdejärjestelmänIdllä, KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, varsinaisSuomiPalvelukäyttäjä)

      val oid = kirjoitettuOo.oid.get

      mitätöiOpiskeluoikeus(oid, MockUsers.paakayttaja)

      putOpiskeluoikeus(ooLähdejärjestelmänIdllä, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.suostumusPeruttu())
      }
    }


    "Koskeen ei voi päivittää oidin kautta samaa opiskeluoikeutta, joka on jo mitätöity" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)

      val oo = defaultOpiskeluoikeus.copy(
        oid = Some(ooOid)
      )

      putOpiskeluoikeus(oo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${ooOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      val ooOid: String = setupOppijaWithAndGetOpiskeluoikeus(
        oo = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      ).oid.get

      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession).get.length should equal (1)

      mitätöiOpiskeluoikeus(ooOid, MockUsers.paakayttaja)

      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession) should equal (None)
    }
  }

  "Kun suostumus voidaan peruuttaa ja opiskeluoikeus mitätöidään put-routella" - {
    def vapaatavoitteinenOpiskeluoikeusMitätöity(oo: VapaanSivistystyönOpiskeluoikeus) = {
      val opiskeluoikeusjaksotMitätöity = oo.tila.opiskeluoikeusjaksot ++
        Seq(OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
          alku = oo.tila.opiskeluoikeusjaksot.head.alku,
          tila = opiskeluoikeusMitätöity
        ))
      oo.copy(tila = oo.tila.copy(opiskeluoikeusjaksot = opiskeluoikeusjaksotMitätöity))
    }

    "Opiskeluoikeus on poistunut" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      authGet("api/opiskeluoikeus/" + ooOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Mitätöinnistä jää audit-log -merkintä" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      AuditLogTester.clearMessages
      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val logMessages = AuditLogTester.getLogMessages
      logMessages.length should equal(1)

      val vapaatavoitteinenOpiskeluoikeusRow: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          ooOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      AuditLogTester.verifyAuditLogString(logMessages.head, Map(
        "operation" -> KoskiOperation.OPISKELUOIKEUS_MUUTOS.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid,
          KoskiAuditLogMessageField.opiskeluoikeusId.toString -> vapaatavoitteinenOpiskeluoikeusRow.map(_.id).getOrElse("").toString,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> (2).toString
        ),
      ))
    }

    "Opiskeluoikeudesta jää raatorivi opiskeluoikeustauluun" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          ooOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      result.isRight should be(true)

      result.map(ooRow => {
        ooRow.oid should be(ooOid)
        ooRow.versionumero should be(2)
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
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )

      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksiaEnnenPerumistaOpenSearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      KoskiApplicationForTests.perustiedotIndexer.sync(true)
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaOpenSearchissa-1)
    }

    "Mitätöinnin jälkeen pääkäyttäjä näkee mitätöinnin" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      val mitätöityOo = vapaatavoitteinenOpiskeluoikeusMitätöity(oo)
      putOpiskeluoikeus(mitätöityOo, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }
      val loginHeaders = authHeaders(MockUsers.paakayttaja)

      get(s"/api/opiskeluoikeus/suostumuksenperuutus", headers = loginHeaders) {
        verifyResponseStatusOk()

        val json = parse(body)
        val obj = json(0)

        (obj \\ "Opiskeluoikeuden oid") shouldBe JString(ooOid)
        (obj \\ "Oppijan oid") shouldBe JString(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
        (obj \\ "Opiskeluoikeuden päättymispäivä") shouldBe JString(mitätöityOo.tila.opiskeluoikeusjaksot.last.alku.toString)
        (obj \\ "Mitätöity") shouldBe a[JString]
        (obj \\ "Suostumus peruttu") should not be a[JString]
        (obj \\ "Oppilaitoksen oid") shouldBe JString(oo.oppilaitos.get.oid)
        (obj \\ "Oppilaitoksen nimi") shouldBe JString(oo.oppilaitos.get.nimi.get.get("fi"))
      }
    }

    "Koskeen ei voida syöttää uudestaan opiskeluoikeutta, jonka lähdejärjestelmän id löytyy peruutetuista" in {
      val oo = defaultOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-32041")),
        oid = None,
        versionumero = None
      )

      val ooOid = setupOppijaWithAndGetOpiskeluoikeus(
        oo,
        henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus,
        headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent
      ).oid.get

      val ooMitätöity =  {
        val opiskeluoikeusjaksotMitätöity = oo.tila.opiskeluoikeusjaksot ++ Seq(OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(alku = oo.tila.opiskeluoikeusjaksot.head.alku, tila = opiskeluoikeusMitätöity))
        oo.copy(
          oid = Some(ooOid),
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
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      val oo2 = defaultOpiskeluoikeus.copy(
        oid = Some(ooOid)
      )

      putOpiskeluoikeus(oo2, henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${ooOid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession).get.length should equal (1)

      putOpiskeluoikeus(vapaatavoitteinenOpiskeluoikeusMitätöity(oo), henkilö = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus, headers = authHeaders(paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet) ++ jsonContent){
        verifyResponseStatusOk()
      }

      // Perumisen jälkeen
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(ooOid)(virkailijaSession) should equal (None)
    }
  }

  "Kun suostumusta ei voida peruuttaa" - {
    "Kansalainen ei voi peruuttaa kenenkään muun suostumusta" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      val loginHeaders = kansalainenLoginHeaders(teijaHetu)

      // API:n yli
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $ooOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
      // Kutsutaan suoraan serviceä
      val teijaSession = sessio(KoskiSpecificMockOppijat.teija.oid)
      KoskiApplicationForTests.suostumuksenPeruutusService.peruutaSuostumus(ooOid, None)(teijaSession).statusCode should equal (403)
    }

    "Vain vapaan sivistystyön vapaatavoitteisen suorituksen ja opiskeluoikeuden voi peruuttaa" in {
      val loginHeaders = kansalainenLoginHeaders(teijaHetu)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$teijaOpiskeluoikeusOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $teijaOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
    }

    "Kansalainen ei voi peruuttaa suostumusta, josta on tehty suoritusjako" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        defaultOpiskeluoikeus,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      val loginHeaders = kansalainenLoginHeaders(vapaatavoitteinenHetu)

      val json =
        raw"""[{
          "oppilaitosOid": "${MockOrganisaatiot.varsinaisSuomenKansanopisto}",
          "suorituksenTyyppi": "vstvapaatavoitteinenkoulutus",
          "koulutusmoduulinTunniste": "099999"
        }]"""

      createSuoritusjako(json, vapaatavoitteinenHetu){
        verifyResponseStatusOk()
      }

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $ooOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
    }

    "Kansalainen ei voi peruuttaa suostumusta, jos opiskeluoikeus on mitätöity" in {
      val oo = setupOppijaWithAndGetOpiskeluoikeus(
        defaultOpiskeluoikeus,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
      )
      val ooOid = oo.oid.get

      mitätöiOpiskeluoikeus(ooOid)

      val loginHeaders = kansalainenLoginHeaders(vapaatavoitteinenHetu)

      // API:n yli
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$ooOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $ooOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa, opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua tai opiskeluoikeudelta ei löytynyt annetun syötteen tyyppistä päätason suoritusta."
        ))
      }
      // Kutsutaan suoraan serviceä
      val vapaatavoitteinenSession = sessio(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)
      KoskiApplicationForTests.suostumuksenPeruutusService.peruutaSuostumus(ooOid, None)(vapaatavoitteinenSession).statusCode should equal (403)
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
