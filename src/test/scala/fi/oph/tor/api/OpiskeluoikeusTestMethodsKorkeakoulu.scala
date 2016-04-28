package fi.oph.tor.api

import fi.oph.tor.documentation.ExamplesKorkeakoulu
import fi.oph.tor.schema._

trait OpiskeluoikeusTestMethodsKorkeakoulu extends OpiskeluOikeusTestMethods[KorkeakoulunOpiskeluoikeus]{
  override def defaultOpiskeluoikeus = ExamplesKorkeakoulu.uusi.opiskeluoikeudet.head.asInstanceOf[KorkeakoulunOpiskeluoikeus]
}
