package fi.oph.koski.api

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSession.systemUser
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.organisaatio.MockOrganisaatiot.{omnia, stadinAmmattiopisto}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, OidOrganisaatio, Oppilaitos, SisältäväOpiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}

import scala.language.reflectiveCalls

class SisältyväOpiskeluoikeusSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with LocalJettyHttpSpecification with DatabaseTestMethods {
  "Sisältyvä opiskeluoikeus" - {
    resetFixtures

    lazy val original = getOpiskeluoikeudet(MockOppijat.sisältyvä.oid).filter(_.getOppilaitos.oid == stadinAmmattiopisto).head.asInstanceOf[AmmatillinenOpiskeluoikeus]
    lazy val sisältyvä = getOpiskeluoikeudet(MockOppijat.sisältyvä.oid).filter(_.getOppilaitos.oid == omnia).head.asInstanceOf[AmmatillinenOpiskeluoikeus]

    lazy val originalId = opiskeluoikeusId(original).get
    lazy val sisältyväId = opiskeluoikeusId(sisältyvä).get

    "Kun sisältävä opiskeluoikeus löytyy Koskesta" - {
      "Lisäys onnistuu" in {
        sisältyvä.oid.isDefined should equal(true)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla on katseluoikeudet sisältyvään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(MockOppijat.sisältyvä.oid, stadinAmmattiopistoTallentaja).flatMap(_.oid)
        oids should contain(original.oid.get)
        oids should contain(sisältyvä.oid.get)
      }

      "Sisältyvän opiskeluoikeuden organisaatiolla ei ole oikeuksia sisältävään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(MockOppijat.sisältyvä.oid, MockUsers.omniaKatselija).flatMap(_.oid)
        oids should contain(sisältyvä.oid.get)
        oids should not contain(original.oid)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla ei ole kirjoitusoikeuksia sisältyvään opiskeluoikeuteen" in {
        putOpiskeluoikeus(sisältyvä, henkilö = MockOppijat.sisältyvä, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon 1.2.246.562.10.51720121923"))
        }
        putOpiskeluoikeus(sisältyvä, henkilö = MockOppijat.sisältyvä, headers = authHeaders(MockUsers.omniaTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "Sisältävän opiskeluoikeuden organisaatio löytää sisältyvän opiskeluoikeuden hakutoiminnolla" in {
        searchForPerustiedot(Map("toimipiste" -> stadinAmmattiopisto), stadinAmmattiopistoTallentaja).map(_.id) should contain(originalId)
        searchForPerustiedot(Map("toimipiste" -> omnia), stadinAmmattiopistoTallentaja).map(_.id) should contain(sisältyväId)
      }

      "Sisältyvän opiskeluoikeuden organisaatio ei löydä sisältävää opiskeluoikeutta hakutoiminnolla" in {
        searchForPerustiedot(Map("toimipiste" -> stadinAmmattiopisto), MockUsers.omniaKatselija).map(_.id) should equal(Nil)
        searchForPerustiedot(Map("toimipiste" -> omnia), MockUsers.omniaKatselija).map(_.id) should contain(sisältyväId)
      }
    }

    "Kun sisältävä opiskeluoikeus ei löydy Koskesta -> HTTP 400" in {
      putOpiskeluoikeus(sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.oppilaitos.get, "66666666")))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy("Sisältävää opiskeluoikeutta ei löydy oid-arvolla 66666666"))
      }
    }

    "Kun sisältävän opiskeluoikeuden organisaatio ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(Oppilaitos(omnia), original.oid.get)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos())
      }
     }

    "Kun sisältävän opiskeluoikeuden henkilötieto ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(sisältyvä, henkilö = MockOppijat.eerola) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
      }
    }

    "Kun sisältävän opiskeluoikeuden henkilötieto on linkitetty -> HTTP 200" in {
      val original = createOpiskeluoikeus(MockOppijat.master, defaultOpiskeluoikeus, user = stadinAmmattiopistoTallentaja)
      val sisältyvä: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        oppilaitos = Some(Oppilaitos(omnia)),
        sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.oppilaitos.get, original.oid.get)),
        suoritukset = List(autoalanPerustutkinnonSuoritus(OidOrganisaatio(omnia)))
      )

      putOpiskeluoikeus(sisältyvä, henkilö = MockOppijat.slave.henkilö) {
        verifyResponseStatusOk()
      }
    }
  }

  def opiskeluoikeusId(oo: AmmatillinenOpiskeluoikeus): Option[Int] =
    oo.oid.flatMap(oid => runDbSync(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === oid).map(_.id).result).headOption)
}
