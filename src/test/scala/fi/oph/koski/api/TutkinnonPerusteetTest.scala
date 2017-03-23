package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

trait TutkinnonPerusteetTest[T <: Opiskeluoikeus] extends FreeSpec with PutOpiskeluoikeusTestMethods[T] {
  "Tutkinnon perusteet" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(200)
        }
      }
      "myös ePerusteista löytymätön, mutta koodistosta \"koskikoulutusdiaarinumerot\" löytyvä diaarinumero kelpaa" in {
        putOpiskeluoikeus(opiskeluoikeusWithPerusteenDiaarinumero(Some(eperusteistaLöytymätönValidiDiaarinumero))) {
          verifyResponseStatus(200)
        }
      }
    }

    "Kun yritetään liittää suoritus tuntemattomaan tutkinnon perusteeseen" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some("39/xxx/2014"))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
      }
    }

    "Kun yritetään liittää suoritus väärään koulutustyyppiin liittyvään perusteeseen" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(vääräntyyppisenPerusteenDiaarinumero))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi(("Perusteella " + vääräntyyppisenPerusteenDiaarinumero + " on väärä koulutustyyppi .* Hyväksytyt koulutustyypit .*").r)))
      }
    }

    "Kun lisätään opiskeluoikeus ilman tutkinnon perusteen diaarinumeroa" - {
      "palautetaan HTTP 200"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(None)) (verifyResponseStatus(200))
      }
    }

    "Kun yritetään lisätä opiskeluoikeus tyhjällä diaarinumerolla" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(""))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*perusteenDiaarinumero.*".r)))
      }
    }
  }

  def vääräntyyppisenPerusteenDiaarinumero: String = "39/011/2014"

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): T

  def eperusteistaLöytymätönValidiDiaarinumero: String

  def putTodistus[A](opiskeluoikeus: T, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(Json.toJValue(opiskeluoikeus))), headers)(f)
  }
}
