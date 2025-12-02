package fi.oph.koski.sdg

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.ytr.MockYtrClient
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.language.reflectiveCalls

class KehaSDGSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    super.afterEach()
    MockYtrClient.reset()
  }

  "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
    AuditLogTester.clearMessages()
    postHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
      verifyResponseStatusOk()
      AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "SDG_OPISKELUOIKEUS_HAKU", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
    }
  }

  "Tuetut opiskeluoikeus- ja suoritustyypit tulevat läpi" in {
    val tuetutTyypit = List(
      (OpiskeluoikeudenTyyppi.ammatillinenkoulutus, classOf[SdgAmmatillisenTutkinnonSuoritus], KoskiSpecificMockOppijat.ammattilainen),
      (OpiskeluoikeudenTyyppi.ammatillinenkoulutus, classOf[SdgAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus], KoskiSpecificMockOppijat.osittainenAmmattitutkintoUseastaTutkinnostaValmis),
      (OpiskeluoikeudenTyyppi.ammatillinenkoulutus, classOf[SdgAmmatillisenTutkinnonOsaTaiOsia], KoskiSpecificMockOppijat.osittainenammattitutkinto),
      (OpiskeluoikeudenTyyppi.diatutkinto, classOf[SdgDIATutkinnonSuoritus], KoskiSpecificMockOppijat.dia),
      (OpiskeluoikeudenTyyppi.diatutkinto, classOf[SdgDIAValmistavanVaiheenSuoritus], KoskiSpecificMockOppijat.dia),
      (OpiskeluoikeudenTyyppi.ebtutkinto, classOf[SdgEBTutkinnonPäätasonSuoritus], KoskiSpecificMockOppijat.europeanSchoolOfHelsinki),
      (OpiskeluoikeudenTyyppi.europeanschoolofhelsinki, classOf[SdgSecondaryLowerVuosiluokanSuoritus], KoskiSpecificMockOppijat.europeanSchoolOfHelsinki),
      (OpiskeluoikeudenTyyppi.europeanschoolofhelsinki, classOf[SdgSecondaryUpperVuosiluokanSuoritus], KoskiSpecificMockOppijat.europeanSchoolOfHelsinki),
      (OpiskeluoikeudenTyyppi.ibtutkinto, classOf[SdgIBTutkinnonSuoritus], KoskiSpecificMockOppijat.ibFinal),
      (OpiskeluoikeudenTyyppi.ibtutkinto, classOf[SdgPreIBSuoritus2015], KoskiSpecificMockOppijat.ibFinal),
      (OpiskeluoikeudenTyyppi.ibtutkinto, classOf[SdgPreIBSuoritus2019], KoskiSpecificMockOppijat.ibPreIB2019),
      (OpiskeluoikeudenTyyppi.korkeakoulutus, classOf[SdgKorkeakoulututkinnonSuoritus], KoskiSpecificMockOppijat.dippainssi),
      (OpiskeluoikeudenTyyppi.lukiokoulutus, classOf[SdgLukionOppimääränSuoritus2015], KoskiSpecificMockOppijat.lukiolainen),
      (OpiskeluoikeudenTyyppi.lukiokoulutus, classOf[SdgLukionOppimääränSuoritus2019], KoskiSpecificMockOppijat.uusiLukio),
      (OpiskeluoikeudenTyyppi.ylioppilastutkinto, classOf[SdgYlioppilastutkinnonSuoritus], KoskiSpecificMockOppijat.ylioppilas),
    )

    tuetutTyypit.foreach { case ( ooTyyppi, expectedSuoritusClass, oppija) =>
      postHetu(oppija.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[SdgOppija](body)

        response.opiskeluoikeudet.find(oo =>
          oo.tyyppi == ooTyyppi &&
            oo.suoritukset.exists(expectedSuoritusClass.isInstance)
        ).getOrElse(
          fail(s"Oppijalta ${oppija.oid} ei löydy suoritusta ${expectedSuoritusClass.getSimpleName}")
        )
      }
    }
  }

  "Rajapinnan parametrit" in {
    val hetu = KoskiSpecificMockOppijat.ammattilainen.hetu.get

    val paramCombinations = List(
      (true, false),
      (false, false),
      (true, true),
      (false, true)
    )

    paramCombinations.foreach { case (osasuorituksetMukaan, vainVahvistetut) =>
      withClue(
        s"osasuoritukset=$osasuorituksetMukaan, vainVahvistetut=$vainVahvistetut"
      ) {
        postHetu(hetu, osasuorituksetMukaan, vainVahvistetut) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[SdgOppija](body)

          val oo = response.opiskeluoikeudet.find(oo =>
            oo.suoritukset.exists(_.tyyppi.koodiarvo == "ammatillinentutkinto")
          ).getOrElse(
            fail(s"Opiskeluoikeutta jolla ammatillinentutkinto ei löydy")
          )

          if (osasuorituksetMukaan && oo.suoritukset.forall {
            _.osasuoritukset.isEmpty
          }) {
            fail("Osasuorituksia ei löydy yhdeltäkään suoritukselta (testidatassa pitäisi olla)")
          }

          if (!osasuorituksetMukaan && oo.suoritukset.exists(s =>
            s.osasuoritukset.exists(_.nonEmpty))) {
            fail("Osasuorituksia mukana vaikkei pitäisi")
          }

          if (vainVahvistetut && oo.suoritukset.exists(_.vahvistus.isEmpty)) {
            fail("Vahvistamaton suoritus mukana vaikka vainVahvistetut=t")
          }

          if (!vainVahvistetut && oo.suoritukset.forall(_.vahvistus.isEmpty)) {
            fail("Vahvistamaton suoritus puuttuu (testidatassa pitäisi olla)")
          }
        }
      }
    }
  }

  "Lukio ei paljasta tunnustettu-kenttää mutta tunnustettuBoolean tulee läpi" in {
    getOppija(KoskiSpecificMockOppijat.lukiolainen.oid) {
      verifyResponseStatusOk()
      body should include ("\"tunnustettu\"")
    }

    postHetu(KoskiSpecificMockOppijat.lukiolainen.hetu.get) {
      verifyResponseStatusOk()
      body should not include ("\"tunnustettu\"")
      body should include("\"tunnustettuBoolean\":true")
      body should include("\"tunnustettuBoolean\":false")
    }
  }

  "ESH ei sisällä jääLuokalle-kenttää" in {
    getOppija(KoskiSpecificMockOppijat.lukiolainen.oid) {
      verifyResponseStatusOk()
      body should include ("\"jääLuokalle\"")
    }
    postHetu(KoskiSpecificMockOppijat.lukiolainen.hetu.get) {
      verifyResponseStatusOk()
      body should not include ("\"jääLuokalle\"")
    }
  }

  private def postHetu[A](
    hetu: String,
    osasuorituksetMukaan: Boolean = true,
    vainVahvistetut: Boolean = false,
    user: MockUser = MockUsers.kehaSdgKäyttäjä
  )(f: => A): A = {
    val url =
      s"api/luovutuspalvelu/keha/sdg/hetu?osasuoritukset=$osasuorituksetMukaan&vainVahvistetut=$vainVahvistetut"

    post(
      url,
      JsonSerializer.writeWithRoot(KehaSdgRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def getOppija[A](
    oppijaOid: String,
    user: MockUser = MockUsers.ophkatselija
  )(f: => A): A = {
    val url =
      s"api/oppija/$oppijaOid"

    get(
      url,
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }
}

case class KehaSdgRequest(hetu: String)
