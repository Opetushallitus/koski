package fi.oph.koski.ytr.download

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.Wait
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.time.LocalDate

trait YtrDownloadTestMethods extends KoskiHttpSpec with OpiskeluoikeusTestMethods {
  implicit val formats = DefaultFormats

  def clearYtrData(): Unit = {
    authGet(s"test/ytr/clear") {
      verifyResponseStatusOk()
    }
  }

  def downloadYtrData(
    birthmonthStart: String,
    birthmonthEnd: String,
    force: Boolean = false
  ): Unit = downloadYtrData(
    birthmonthStart = Some(birthmonthStart),
    birthmonthEnd = Some(birthmonthEnd),
    modifiedSince = None,
    modifiedSinceLastRun = None,
    force = force
  )

  def downloadYtrData(
    modifiedSince: LocalDate,
    force: Boolean
  ): Unit = downloadYtrData(
    birthmonthStart = None,
    birthmonthEnd = None,
    modifiedSince = Some(modifiedSince),
    modifiedSinceLastRun = None,
    force = force
  )

  def downloadYtrData(
    modifiedSinceLastRun: Boolean,
    force: Boolean
  ): Unit = downloadYtrData(
    birthmonthStart = None,
    birthmonthEnd = None,
    modifiedSince = None,
    modifiedSinceLastRun = Some(modifiedSinceLastRun),
    force = force
  )

  private def downloadYtrData(
    birthmonthStart: Option[String],
    birthmonthEnd: Option[String],
    modifiedSince: Option[LocalDate],
    modifiedSinceLastRun: Option[Boolean],
    force: Boolean,
  ): Unit = {
    val urlParams = List(
      Some(s"force=${force}"),
      birthmonthStart.map(d => s"birthmonthStart=$d"),
      birthmonthEnd.map(d => s"birthmonthEnd=$d"),
      modifiedSince.map(d => s"modifiedSince=${d.toString}"),
      modifiedSinceLastRun.map(d => s"modifiedSinceLastRun=${d.toString}")
    ).flatten.mkString("&")

    authGet(s"test/ytr/download?${urlParams}") {
      verifyResponseStatusOk()
    }
    Wait.until(downloadComplete)
  }

  def downloadComplete = authGet("api/ytr/download-status") {
    val isComplete = (JsonMethods.parse(body) \ "current" \ "status").extract[String] == "complete"
    isComplete
  }

  def getDownloadStatusRows() = authGet("test/ytr/download-status-rows") {
    JsonMethods.parse(body).children
  }

  def totalCount: Int = authGet("api/ytr/download-status") {
    (JsonMethods.parse(body) \ "current" \ "totalCount").extract[Int]
  }

  def errorCount: Int = authGet("api/ytr/download-status") {
    (JsonMethods.parse(body) \ "current" \ "errorCount").extract[Int]
  }

  def getYtrOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid + "/ytr-json", user) {
      verifyResponseStatusOk()
      readOppija
    }
  }

  def getYtrOppijaVersionumerolla(oppijaOid: String, versionumero: Int, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid + "/ytr-json/" + versionumero, user) {
      verifyResponseStatusOk()
      readOppija
    }
  }

  def getYtrSavedOriginal(oppijaOid: String, user: UserWithPassword = defaultUser): String = {
    authGet("api/oppija/" + oppijaOid + "/ytr-saved-original-json", user) {
      verifyResponseStatusOk()
      body
    }
  }

  def getYtrCurrentOriginal(oppijaOid: String, user: UserWithPassword = defaultUser): String = {
    authGet("api/oppija/" + oppijaOid + "/ytr-current-original-json", user) {
      verifyResponseStatusOk()
      body
    }
  }
}
