package fi.oph.koski.api

import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import scala.reflect.runtime.universe.TypeTag

trait TutkinnonPerusteetTest[T <: Opiskeluoikeus] extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[T] {
  def tag: TypeTag[T]
  "Tutkinnon perusteet" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
      "myös ePerusteista löytymätön, mutta koodistosta \"koskikoulutusdiaarinumerot\" löytyvä diaarinumero kelpaa" in {
        putOpiskeluoikeus(opiskeluoikeusWithPerusteenDiaarinumero(Some(eperusteistaLöytymätönValidiDiaarinumero))) {
          verifyResponseStatusOk()
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
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(vääräntyyppisenPerusteenDiaarinumero))) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.rakenne.vääräKoulutustyyppi, s".*ei voi käyttää perustetta $vääräntyyppisenPerusteenDiaarinumero \\($vääräntyyppisenPerusteenId\\), jonka koulutustyyppi on .*. Tälle suoritukselle hyväksytyt perusteen koulutustyypit ovat.*".r)))
      }
    }

    "Kun yritetään lisätä opiskeluoikeus tyhjällä diaarinumerolla" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(""))) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*perusteenDiaarinumero.*".r)))
      }
    }
  }

  def vääräntyyppisenPerusteenDiaarinumero: String = "39/011/2014"
  def vääräntyyppisenPerusteenId: Long = 612

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): T

  def eperusteistaLöytymätönValidiDiaarinumero: String

  def putTodistus[A](opiskeluoikeus: T, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(JsonSerializer.serializeWithRoot(opiskeluoikeus)(tag))), headers)(f)
  }
}
