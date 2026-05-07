package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AhvenanmaanPerusopetusExampleData
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationAhvenanmaanPerusopetusSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with PutOpiskeluoikeusTestMethods[AhvenanmaanPerusopetuksenOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = AhvenanmaanPerusopetusExampleData.opiskeluoikeus

  "Ahvenanmaan perusopetuksen opiskeluoikeus" - {
    "voidaan tallentaa paikallisesti" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
  }
}
