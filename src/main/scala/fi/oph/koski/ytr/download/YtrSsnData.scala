package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.util.DateOrdering.{localDateOptionOrdering, localDateOrdering}

import java.time.format.DateTimeFormatter


case class YtrSsnData(
  ssns: Option[List[String]]
) {
  private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
  private def asSortedBirthdays = sortedByBirthdays.ssns.map(xs => xs.flatMap(x => Hetu.toBirthday(x).toList))

  def minMonth: String = asSortedBirthdays.map(xs => xs.head.format(monthFormatter)).getOrElse("-")
  def maxMonth: String = asSortedBirthdays.map(xs => xs.last.format(monthFormatter)).getOrElse("-")
  def sortedByBirthdays: YtrSsnData =
    copy(ssns = ssns.map(_.sortBy(Hetu.toBirthday)(localDateOptionOrdering)))
}
