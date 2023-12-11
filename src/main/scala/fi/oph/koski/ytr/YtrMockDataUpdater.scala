package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonFiles

object YtrMockDataUpdater extends App {
  List("250493-602S", "200695-889X").foreach { hetu =>
    YtrClient(KoskiApplication.defaultConfig).oppijaJsonByHetu(YtrSsnWithPreviousSsns(hetu)) match {
      case None => throw new IllegalStateException("Oppijaa ei löydy: " + hetu)
      case Some(oppija) => JsonFiles.writeFile(MockYtrClient.filename(hetu), oppija)
    }
  }
}
