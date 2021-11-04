package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.util.Timing

trait ValpasRouhintaTiming extends Timing {
  def rouhintaTimed[R](blocknamePrefix: String, size: Int)(block: => R): R = {
    val blockname = size match {
      case n if n <= 1000 => s"${blocknamePrefix}1To1000"
      case n if n <= 10000 => s"${blocknamePrefix}1001To10000"
      case n if n <= 50000 => s"${blocknamePrefix}10001To50000"
      case n if n <= 100000 => s"${blocknamePrefix}50000To100000"
      case _ => s"${blocknamePrefix}Over100000"
    }

    timed[R](blockname)(block)
  }
}
