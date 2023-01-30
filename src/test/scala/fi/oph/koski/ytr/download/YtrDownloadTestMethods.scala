package fi.oph.koski.ytr.download

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.util.Wait
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import java.time.LocalDate

trait YtrDownloadTestMethods extends KoskiHttpSpec {
  implicit val formats = DefaultFormats

  def redownloadYtrData(
    birthdateStart: LocalDate,
    birthdateEnd: LocalDate,
    force: Boolean = false
  ): Unit = redownloadYtrData(
    birthdateStart = Some(birthdateStart),
    birthdateEnd = Some(birthdateEnd),
    modifiedSince = None,
    force = force
  )

  def redownloadYtrData(
    birthdateStart: Option[LocalDate],
    birthdateEnd: Option[LocalDate],
    modifiedSince: Option[LocalDate],
    force: Boolean,
  ): Unit = {
    val urlParams = List(
      Some(s"force=${force}"),
      birthdateStart.map(d => s"birthdateStart=${d.toString}"),
      birthdateEnd.map(d => s"birthdateEnd=${d.toString}"),
      modifiedSince.map(d => s"modifiedSince=${d.toString}")
    ).flatten.mkString("&")

    authGet(s"test/ytr/download?${urlParams}") {
      verifyResponseStatusOk()
    }
    Wait.until(downloadComplete)
  }

  def downloadComplete = authGet("test/ytr/download-status") {
    val isComplete = (JsonMethods.parse(body) \ "status").extract[String] == "done"
    isComplete
  }
}
