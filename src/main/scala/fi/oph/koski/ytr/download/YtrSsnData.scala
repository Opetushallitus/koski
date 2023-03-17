package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.util.DateOrdering.{localDateOptionOrdering, localDateOrdering}

import java.time.format.DateTimeFormatter


case class YtrSsnData(
  ssns: Option[List[String]]
) {
  private def formatOptionalBirthday(o: Option[String]): String = o.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM"))).getOrElse("-")
  private def minAndMaxMonth: (String, String) = ssnsSortedByBirthdays.map(xs => {
    (formatOptionalBirthday(xs.headOption), formatOptionalBirthday(xs.lastOption))
  }).getOrElse(("-", "-"))
  def minMonth: String = minAndMaxMonth._1
  def maxMonth: String = minAndMaxMonth._2
  def ssnsWithValidFormat: Option[List[String]] = ssns.map(p => p.filter(ssn => Hetu.validFormat(ssn).isRight))
  def ssnsSortedByBirthdays: Option[List[String]] = ssnsWithValidFormat.map(_.sortBy(Hetu.toBirthday)(localDateOptionOrdering))
}
