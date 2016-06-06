package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json

object YtrMockDataUpdater extends App {
  List("250493-602S", "200695-889X").foreach { hetu =>
    YlioppilasTutkintoRekisteri(KoskiApplication.defaultConfig).oppijaJsonByHetu(hetu) match {
      case None => throw new IllegalStateException("Oppijaa ei löydy: " + hetu)
      case Some(oppija) => Json.writeFile(YtrMock.filename(hetu), oppija)
    }
  }
}
