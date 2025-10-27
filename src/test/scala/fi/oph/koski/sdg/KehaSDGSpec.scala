package fi.oph.koski.sdg

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
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

  "Keha SDG rajapinta" - {
    "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "SDG_OPISKELUOIKEUS_HAKU", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }

    "Ammatillinen tulee läpi osasuorituksineen" in {
      postHetu(KoskiSpecificMockOppijat.ammattilainen.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.ammattilainen.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ammatillinenkoulutus"))
        response.opiskeluoikeudet.flatMap(_.suoritukset.map(_.tyyppi.koodiarvo)) should equal(List("ammatillinentutkinto"))
        response.opiskeluoikeudet.flatMap(_.suoritukset.map(_.osasuoritukset.size)) should equal(List(1))
      }
    }

    "Ammatillisen osa/osia tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.osittainenammattitutkinto.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.osittainenammattitutkinto.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ammatillinenkoulutus"))
        response.opiskeluoikeudet.flatMap(_.suoritukset.map(_.tyyppi.koodiarvo)) should equal(List("ammatillinentutkintoosittainen"))
      }
    }

    "Ammatillisen osia useasta tutkinnosta tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.osittainenAmmattitutkintoUseastaTutkinnostaValmis.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.osittainenAmmattitutkintoUseastaTutkinnostaValmis.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ammatillinenkoulutus"))
        response.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.koodiarvo)) should equal(List("ammatillinentutkintoosittainenuseastatutkinnosta"))
      }
    }

    "Korkeakoulu tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.dippainssi.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.dippainssi.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("korkeakoulutus"))
      }
    }

    "EB tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ebtutkinto"))
      }
    }

    "DIA tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.dia.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.dia.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("diatutkinto"))
      }
    }

    "YO tulee läpi" in {
      postHetu(KoskiSpecificMockOppijat.ylioppilasUusiApi.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Oppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.ylioppilasUusiApi.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ylioppilastutkinto"))
        response.opiskeluoikeudet.flatMap(_.suoritukset.map(_.tyyppi.koodiarvo)) should equal(List("ylioppilastutkinto"))

        val koodiarvot =
          for {
            oo <- response.opiskeluoikeudet.asInstanceOf[List[YlioppilastutkinnonOpiskeluoikeus]]
            suoritus <- oo.suoritukset
            osasuoritus <- suoritus.osasuoritukset.getOrElse(Nil)
          } yield osasuoritus.koulutusmoduuli.tunniste.koodiarvo

        koodiarvot should equal(List("EA", "EA", "EA", "YH", "YH", "YH", "N", "N", "N", "A", "A", "A", "A"))
      }
    }
  }

  private def postHetu[A](hetu: String, user: MockUser = MockUsers.kehaSdgKäyttäjä)(f: => A): A = {
    post(
      "api/luovutuspalvelu/keha/sdg/hetu?includeOsasuoritukset=true",
      JsonSerializer.writeWithRoot(KehaSdgRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }


}

case class KehaSdgRequest(hetu: String)
