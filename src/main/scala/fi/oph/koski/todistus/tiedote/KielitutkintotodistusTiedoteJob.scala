package fi.oph.koski.todistus.tiedote

import java.time.LocalDateTime

case class KielitutkintotodistusTiedoteJob(
  id: String,
  oppijaOid: String,
  opiskeluoikeusOid: String,
  state: String,
  createdAt: LocalDateTime = LocalDateTime.now(),
  completedAt: Option[LocalDateTime] = None,
  worker: Option[String] = None,
  attempts: Int = 0,
  error: Option[String] = None,
  opiskeluoikeusVersio: Int = 0
)

object KielitutkintotodistusTiedoteState {
  val COMPLETED = "COMPLETED"
  val ERROR = "ERROR"
}
