package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresTilastokeskus
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}

class TilastokeskusServlet(implicit val application: KoskiApplication) extends ApiServlet with ObservableSupport with NoCache with OpiskeluoikeusQueries with RequiresTilastokeskus {
  get("/") {
    if (!getOptionalIntegerParam("v").contains(1)) {
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
    }
    queryAndStreamOpiskeluoikeudet
  }

  override protected val maxNumberOfItemsPerPage: Int = 1000
}
