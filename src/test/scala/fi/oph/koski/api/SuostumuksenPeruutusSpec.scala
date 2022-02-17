package fi.oph.koski.api

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.koskiuser.MockUsers.varsinaisSuomiPalvelukäyttäjä
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus
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
  val vapaatavoitteinenOpiskeluoikeusOid = vapaatavoitteinenOpiskeluoikeus.oid.get

  val teijaHetu= KoskiSpecificMockOppijat.teija.hetu.get
  val teijaOpiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.teija.oid).head.oid.get

  override def beforeAll = resetFixtures

  "Kun suostumus voidaan peruuttaa" - {
    val opiskeluoikeuksiaEnnenPerumistaElasticsearchissa = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length

    "Opiskeluoikeus on poistunut" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      authGet("api/opiskeluoikeus/" + vapaatavoitteinenOpiskeluoikeusOid, defaultUser) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "Opiskeluoikeus on poistunut Elasticsearchista" in {
      resetFixtures()
      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
      val opiskeluoikeuksia = searchForPerustiedot(Map("toimipiste" -> defaultOpiskeluoikeus.oppilaitos.get.oid), varsinaisSuomiPalvelukäyttäjä).length
      opiskeluoikeuksia should equal (opiskeluoikeuksiaEnnenPerumistaElasticsearchissa-1)
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

    "Opiskeluoikeuden historiatiedot poistuvat" in {
      resetFixtures()
      val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      // Ennen perumista
      KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(vapaatavoitteinenOpiskeluoikeusOid)(virkailijaSession).get.length should equal (1)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$vapaatavoitteinenOpiskeluoikeusOid", headers = kansalainenLoginHeaders(vapaatavoitteinenHetu)) {}
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
          s"Opiskeluoikeuden $vapaatavoitteinenOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua."))
      }
      // Kutsutaan suoraan serviceä
      val teijaSession = sessio(KoskiSpecificMockOppijat.teija.oid)
      KoskiApplicationForTests.suostumuksenPeruutusService.peruutaSuostumus(vapaatavoitteinenOpiskeluoikeusOid)(teijaSession).statusCode should equal (403)
    }

    "Vain vapaan sivistystyön vapaatavoitteisen suorituksen ja opiskeluoikeuden voi peruuttaa" in {
      val loginHeaders = kansalainenLoginHeaders(teijaHetu)

      post(s"/api/opiskeluoikeus/suostumuksenperuutus/$teijaOpiskeluoikeusOid", headers = loginHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeusEiSopivaSuostumuksenPerumiselle(
          s"Opiskeluoikeuden $teijaOpiskeluoikeusOid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua."))
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
          s"Opiskeluoikeuden $oid annettu suostumus ei ole peruttavissa. Joko opiskeluoikeudesta on tehty suoritusjako, viranomainen on käyttänyt opiskeluoikeuden tietoja päätöksenteossa tai opiskeluoikeus on tyyppiä, jonka kohdalla annettua suostumusta ei voida perua."))
      }
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
}
