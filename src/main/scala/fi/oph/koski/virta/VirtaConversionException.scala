package fi.oph.koski.virta

import fi.oph.koski.util.NonCriticalException

case class VirtaConversionException(msg: String) extends NonCriticalException(msg)

