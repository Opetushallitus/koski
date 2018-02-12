package fi.oph.koski.util

import fi.oph.koski.log.NotLoggable
import org.scalatest.{FreeSpec, Matchers}

class InvocationSpec extends FreeSpec with Matchers {
  "Invocation.toString" in {
    Invocation(classOf[String].getMethods()(0), List("hello", List(1,2,3)), "some target object").toString should equal("equals(\"hello\", _)")
  }
  "Invocation.toString respects Loggable" in {
    Invocation(classOf[String].getMethods()(0), List("hello", SomeSecret("secret")), "some target object").toString should equal("equals(\"hello\", *)")
  }
}

case class SomeSecret(secret: String) extends NotLoggable {}
