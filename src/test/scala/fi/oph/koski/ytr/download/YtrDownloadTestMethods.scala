package fi.oph.koski.ytr.download

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.util.Wait
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.time.LocalDate

trait YtrDownloadTestMethods extends KoskiHttpSpec {
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
    force = force
  )

  def downloadYtrData(
    modifiedSince: LocalDate,
    force: Boolean
  ): Unit = downloadYtrData(
    birthmonthStart = None,
    birthmonthEnd = None,
    modifiedSince = Some(modifiedSince),
    force = force
  )

  private def downloadYtrData(
    birthmonthStart: Option[String],
    birthmonthEnd: Option[String],
    modifiedSince: Option[LocalDate],
    force: Boolean,
  ): Unit = {
    val urlParams = List(
      Some(s"force=${force}"),
      birthmonthStart.map(d => s"birthmonthStart=$d"),
      birthmonthEnd.map(d => s"birthmonthEnd=$d"),
      modifiedSince.map(d => s"modifiedSince=${d.toString}")
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
}
