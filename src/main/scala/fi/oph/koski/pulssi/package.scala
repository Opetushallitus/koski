package fi.oph.koski

import scala.math.BigDecimal.RoundingMode.HALF_UP

package object pulssi {
  val round: Int => Double => Double =
    precision => x => BigDecimal(x).setScale(precision, HALF_UP).toDouble
}