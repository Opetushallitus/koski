package fi.oph.koski.opiskeluoikeus

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OidGeneratorSpec extends AnyFreeSpec with Matchers {
  "YTR-generointi tuottaa Oideja oikealta väliltä" in {
    val generator = new OidGenerator

    for( _ <- 1 to 1000){
      val oid = generator.generateYtrOid("1.2.246.562.24.11111111111")
      oid should fullyMatch regex """1.2.246.562.51.[123]\d{10}"""
    }
  }
}
