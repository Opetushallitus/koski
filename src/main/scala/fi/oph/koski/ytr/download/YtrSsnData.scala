package fi.oph.koski.ytr.download

case class YtrSsnData(
  ssns: Option[List[String]]
) {
  def asMonthStrings = ssns.getOrElse(List()).map(x => x.substring(0, 5))
}
