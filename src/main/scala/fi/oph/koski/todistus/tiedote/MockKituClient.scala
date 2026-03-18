package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging

class MockKituClient extends KituClient with Logging {
  override def getExamineeDetails(opiskeluoikeusOid: String): Either[HttpStatus, KituExamineeDetails] = {
    logger.info(s"MockKituClient: getExamineeDetails opiskeluoikeusOid=$opiskeluoikeusOid")
    Right(KituExamineeDetails(
      sukunimi = "Meikäläinen",
      etunimet = "Matti Johannes",
      katuosoite = Some("Esimerkkikatu 123"),
      postinumero = Some("00100"),
      postitoimipaikka = Some("Helsinki"),
      maa = Some(KituKoodiarvo("FIN", "maatjavaltiot1")),
      email = Some("matti.meikalainen@example.com"),
      todistuskieli = Some(KituKoodiarvo("FI", "kieli"))
    ))
  }
}
