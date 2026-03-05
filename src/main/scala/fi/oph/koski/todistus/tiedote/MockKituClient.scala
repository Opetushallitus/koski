package fi.oph.koski.todistus.tiedote

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging

class MockKituClient extends KituClient with Logging {
  override def getExamineeDetails(oppijaOid: String): Either[HttpStatus, KituExamineeDetails] = {
    logger.info(s"MockKituClient: getExamineeDetails oppijaOid=$oppijaOid")
    Right(KituExamineeDetails(preferredLanguage = None, postalAddress = None))
  }
}
