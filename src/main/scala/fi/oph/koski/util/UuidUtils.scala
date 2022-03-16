package fi.oph.koski.util

import java.util.UUID

object UuidUtils {
  def optionFromString(str: String): Option[UUID] = {
    try {
      Some(UUID.fromString(str))
    } catch {
      case _: IllegalArgumentException => None
    }
  }
}
