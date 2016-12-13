package fi.oph.koski.documentation

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, KoskiBaseServlet}

class KoodistoServlet(val koodistoPalvelu: KoodistoPalvelu) extends ApiServlet with Unauthenticated with KoodistoFinder {
  private lazy val koodiarvot = Examples.examples.flatMap(_.data.opiskeluoikeudet).flatMap(_.suoritukset).map(_.tyyppi.koodiarvo).distinct.sorted

  get("/:name/:version") {
    contentType = "application/json"
    val koodit: Option[List[KoodistoKoodi]] = findKoodisto.map(_._2)

    renderOption(KoskiErrorCategory.notFound.koodistoaEiLöydy)(koodit)
  }

  get("/suoritustyypit") {
    koodistoPalvelu.getLatestVersion("suorituksentyyppi").flatMap(koodistoPalvelu.getKoodistoKoodit).get.filter(koodi => koodiarvot.contains(koodi.koodiArvo))
  }
}

trait KoodistoFinder extends KoskiBaseServlet {
  def koodistoPalvelu: KoodistoPalvelu

  def findKoodisto: Option[(KoodistoViite, List[KoodistoKoodi])] = {
    val koodistoUri: String = params("name")
    val versio: Option[KoodistoViite] = params("version") match {
      case "latest" =>
        koodistoPalvelu.getLatestVersion(koodistoUri)
      case _ =>
        Some(KoodistoViite(koodistoUri, getIntegerParam("version")))
    }
    versio.flatMap{ koodisto =>
      koodistoPalvelu.getKoodistoKoodit(koodisto).map { koodit =>
        (koodisto, koodit)
      }
    }
  }
}
