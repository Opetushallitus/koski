package fi.oph.koski.util

object CaseClass {
  def getFieldValue[T](obj: Object, key: String): Option[T] =
    try {
      Some(obj
        .getClass
        .getDeclaredMethod(key)
        .invoke(obj)
        .asInstanceOf[T])
    } catch {
      case _: NoSuchMethodException => None
    }
}
