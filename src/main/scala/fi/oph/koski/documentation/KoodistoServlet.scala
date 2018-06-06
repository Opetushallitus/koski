package fi.oph.koski.documentation

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, KoskiBaseServlet, NoCache}

import scala.collection.immutable.Seq

class KoodistoServlet(implicit val application: KoskiApplication) extends ApiServlet with Unauthenticated with KoodistoFinder with NoCache {
  private val opiskeluoikeudet: Seq[Opiskeluoikeus] = Examples.examples.flatMap(_.data.opiskeluoikeudet)
  private val koodiarvot: Seq[Opiskeluoikeus] => Seq[String] = opiskeluoikeudet => opiskeluoikeudet.flatMap(_.suoritukset).map(_.tyyppi.koodiarvo).distinct.sorted

  get("/:name/:version") {
    contentType = "application/json"
    val koodit: Option[List[KoodistoKoodi]] = findKoodisto.map(_._2)

    renderOption(KoskiErrorCategory.notFound.koodistoaEiLöydy)(koodit)
  }

  get("/suoritustyypit") {
    val opiskeluoikeudenTyyppi = params.get("opiskeluoikeudentyyppi").map(tyyppi => opiskeluoikeudet.filter(oo => oo.tyyppi.koodiarvo == tyyppi)).getOrElse(opiskeluoikeudet)
    koodistoPalvelu.getKoodistoKoodit(koodistoPalvelu.getLatestVersionRequired("suorituksentyyppi"))
      .filter(koodi => koodiarvot(opiskeluoikeudenTyyppi).contains(koodi.koodiArvo))
      .filterNot(_.koodiArvo == "perusopetuksenvuosiluokka")
  }

  def koodistoPalvelu: KoodistoPalvelu = application.koodistoPalvelu
}

trait KoodistoFinder extends KoskiBaseServlet {
  def koodistoPalvelu: KoodistoPalvelu

  def findKoodisto: Option[(KoodistoViite, List[KoodistoKoodi])] = {
    val koodistoUri: String = params("name")
    val versio: Option[KoodistoViite] = params("version") match {
      case "latest" =>
        koodistoPalvelu.getLatestVersionOptional(koodistoUri)
      case _ =>
        Some(KoodistoViite(koodistoUri, getIntegerParam("version")))
    }
    versio.map { koodisto => (koodisto, koodistoPalvelu.getKoodistoKoodit(koodisto)) }
  }
}
