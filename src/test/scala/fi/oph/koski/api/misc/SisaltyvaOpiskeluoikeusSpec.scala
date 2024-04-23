package fi.oph.koski.api.misc

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.systemUser
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja
import fi.oph.koski.organisaatio.MockOrganisaatiot.{omnia, stadinAmmattiopisto}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, OidOrganisaatio, Oppilaitos, SisältäväOpiskeluoikeus}
import fi.oph.koski.util.Wait
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.language.reflectiveCalls

class SisältyväOpiskeluoikeusSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with KoskiHttpSpec with DatabaseTestMethods {
  "Sisältyvä opiskeluoikeus" - {
    lazy val fixture = new {
      resetFixtures
      val original: AmmatillinenOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, defaultHenkilö, headers = authHeaders(stadinAmmattiopistoJaOppisopimuskeskusTallentaja) ++ jsonContent)

      val sisältyvä: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        oppilaitos = Some(Oppilaitos(omnia)),
        sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.oppilaitos.get, original.oid.get)),
        suoritukset = List(autoalanPerustutkinnonSuoritus(OidOrganisaatio(omnia)))
      )
    }

    "Kun sisältävä opiskeluoikeus löytyy Koskesta" - {
      lazy val sisältyvä = createOpiskeluoikeus(defaultHenkilö, fixture.sisältyvä, user = MockUsers.omniaTallentaja)
      "Lisäys onnistuu" in {
        sisältyvä.oid.isDefined should equal(true)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla on katseluoikeudet sisältyvään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(KoskiSpecificMockOppijat.eero.oid, stadinAmmattiopistoJaOppisopimuskeskusTallentaja).flatMap(_.oid)
        oids should contain(fixture.original.oid.get)
        oids should contain(sisältyvä.oid.get)
      }

      "Sisältyvän opiskeluoikeuden organisaatiolla ei ole oikeuksia sisältävään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(KoskiSpecificMockOppijat.eero.oid, MockUsers.omniaKatselija).flatMap(_.oid)
        oids should contain(sisältyvä.oid.get)
        oids should not contain(fixture.original.oid)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla ei ole kirjoitusoikeuksia sisältyvään opiskeluoikeuteen" in {
        putOpiskeluoikeus(sisältyvä, headers = authHeaders(stadinAmmattiopistoJaOppisopimuskeskusTallentaja) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
        }
        putOpiskeluoikeus(sisältyvä, headers = authHeaders(MockUsers.omniaTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "Sisältävän opiskeluoikeuden organisaatio löytää sisältyvän opiskeluoikeuden hakutoiminnolla" in {
        val originalId = opiskeluoikeusId(fixture.original).get
        val sisältyväId = opiskeluoikeusId(sisältyvä).get
        syncPerustiedotToOpenSearch(searchForPerustiedot(Map("toimipiste" -> stadinAmmattiopisto), stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
          .map(_.id).contains(originalId) && searchForPerustiedot(Map("toimipiste" -> omnia), stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
          .map(_.id).contains(sisältyväId))
        searchForPerustiedot(Map("toimipiste" -> stadinAmmattiopisto), stadinAmmattiopistoJaOppisopimuskeskusTallentaja).map(_.id) should contain(originalId)
        searchForPerustiedot(Map("toimipiste" -> omnia), stadinAmmattiopistoJaOppisopimuskeskusTallentaja).map(_.id) should contain(sisältyväId)
      }
    }

    "Kun sisältävä opiskeluoikeus ei löydy Koskesta -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(fixture.original.oppilaitos.get, "66666666")))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy("Sisältävää opiskeluoikeutta ei löydy oid-arvolla 66666666"))
      }
    }

    "Kun sisältävän opiskeluoikeuden organisaatio ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(Oppilaitos(omnia), fixture.original.oid.get)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos())
      }
     }

    "Kun sisältävän opiskeluoikeuden henkilötieto ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä, henkilö = KoskiSpecificMockOppijat.eerola) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
      }
    }

    "Kun sisältävän opiskeluoikeuden henkilötieto on linkitetty -> HTTP 200" in {
      val original = createOpiskeluoikeus(KoskiSpecificMockOppijat.master, defaultOpiskeluoikeus, user = stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
      val sisältyvä: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        oppilaitos = Some(Oppilaitos(omnia)),
        sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.oppilaitos.get, original.oid.get)),
        suoritukset = List(autoalanPerustutkinnonSuoritus(OidOrganisaatio(omnia)))
      )

      putOpiskeluoikeus(sisältyvä, henkilö = KoskiSpecificMockOppijat.slave.henkilö) {
        verifyResponseStatusOk()
      }
    }
  }

  def opiskeluoikeusId(oo: AmmatillinenOpiskeluoikeus): Option[Int] =
    oo.oid.flatMap(oid => runDbSync(KoskiOpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === oid).map(_.id).result).headOption)

  private def syncPerustiedotToOpenSearch(waitCondition: => Boolean): Unit = {
    KoskiApplicationForTests.perustiedotIndexer.sync(refresh = true)
    Wait.until(waitCondition, timeoutMs = 120000)
  }
}
