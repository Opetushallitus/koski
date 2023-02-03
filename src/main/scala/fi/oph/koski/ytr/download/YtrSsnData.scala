package fi.oph.koski.ytr.download

import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.util.DateOrdering.localDateOrdering

import java.time.format.DateTimeFormatter


case class YtrSsnData(
  ssns: Option[List[String]]
) {
  private lazy val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
  private lazy val asBirthdays = ssns.map(xs => xs.flatMap(x => Hetu.toBirthday(x).toList).sorted(localDateOrdering))
  lazy val minMonth: String = asBirthdays.map(xs => xs.head.format(monthFormatter)).getOrElse("-")
  lazy val maxMonth: String = asBirthdays.map(xs => xs.last.format(monthFormatter)).getOrElse("-")
}
