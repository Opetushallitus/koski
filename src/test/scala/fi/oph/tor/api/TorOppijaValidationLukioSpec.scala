package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.schema._
import org.scalatest.FunSpec

class TorOppijaValidationLukioSpec extends FunSpec with OpiskeluoikeusTestMethodsLukio {
   describe("Peruskoulutuksen opiskeluoikeuden lisääminen") {
     describe("Valideilla tiedoilla") {
       it("palautetaan HTTP 200") {
         putOpiskeluOikeus(defaultOpiskeluoikeus) {
           verifyResponseStatus(200)
         }
       }
     }

     describe("Tutkinnon perusteet ja rakenne") {
       describe("Kun yritetään lisätä suoritus tuntemattomaan tutkinnon perusteeseen") {
         it("palautetaan HTTP 400 virhe" ) {
           val suoritus = päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("39/xxx/2014")))
           putTodistus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
         }
       }

       describe("Kun yritetään lisätä suoritus ei-lukiokoulutukselliseen tutkinnon perusteeseen") {
         it("palautetaan HTTP 400 virhe" ) {
           val suoritus = päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("39/011/2014")))
           putTodistus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi("Perusteella 39/011/2014 on väärä koulutustyyppi")))
         }
       }

       describe("Kun lisätään opinto-oikeus ilman tutkinnon perusteen diaarinumeroa") {
         it("palautetaan HTTP 200" ) {
           val suoritus = päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = None))
           putTodistus(suoritus) (verifyResponseStatus(200))
         }
       }

       describe("Kun yritetään lisätä opinto-oikeus tyhjällä diaarinumerolla") {
         it("palautetaan HTTP 400 virhe" ) {
           val suoritus = päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("")))

           putTodistus(suoritus) (verifyResponseStatus(400, TorErrorCategory.badRequest.validation.jsonSchema(".*perusteenDiaarinumero.*".r)))
         }
       }
     }
   }

   def putTodistus[A](suoritus: LukionOppimääränSuoritus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
     val opiskeluOikeus = defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))

     putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluOikeus))), headers)(f)
   }
 }
