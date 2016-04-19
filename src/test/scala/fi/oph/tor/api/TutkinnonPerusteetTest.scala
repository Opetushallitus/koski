package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.schema._
import org.scalatest.FunSpec

trait TutkinnonPerusteetTest[T <: Opiskeluoikeus] extends FunSpec with OpiskeluOikeusTestMethods[T] {
  describe("Tutkinnon perusteet") {
    describe("Valideilla tiedoilla") {
      it("palautetaan HTTP 200") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(200)
        }
      }
    }

    describe("Kun yritetään liittää suoritus tuntemattomaan tutkinnon perusteeseen") {
      it("palautetaan HTTP 400 virhe" ) {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some("39/xxx/2014"))) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
      }
    }

    describe("Kun yritetään liittää suoritus väärään koulutustyyppiin liittyvään perusteeseen") {
      it("palautetaan HTTP 400 virhe" ) {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(vääräntyyppisenPerusteenDiaarinumero))) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Perusteella " + vääräntyyppisenPerusteenDiaarinumero + " on väärä koulutustyyppi")))
      }
    }

    describe("Kun lisätään opinto-oikeus ilman tutkinnon perusteen diaarinumeroa") {
      it("palautetaan HTTP 200" ) {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(None)) (verifyResponseStatus(200))
      }
    }

    describe("Kun yritetään lisätä opinto-oikeus tyhjällä diaarinumerolla") {
      it("palautetaan HTTP 400 virhe" ) {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(""))) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*perusteenDiaarinumero.*".r)))
      }
    }
  }

  def vääräntyyppisenPerusteenDiaarinumero: String = "39/011/2014"

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): T

  def putTodistus[A](opiskeluoikeus: T, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluoikeus))), headers)(f)
  }
}
