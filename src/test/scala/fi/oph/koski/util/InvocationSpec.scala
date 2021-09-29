package fi.oph.koski.util

import fi.oph.koski.TestEnvironment
import fi.oph.koski.log.NotLoggable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class InvocationSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Invocation.toString" in {
    Invocation(classOf[String].getMethods()(0), List("hello", List(1,2,3)), "some target object").toString should equal("equals(\"hello\", _)")
  }
  "Invocation.toString respects Loggable" in {
    Invocation(classOf[String].getMethods()(0), List("hello", SomeSecret("secret")), "some target object").toString should equal("equals(\"hello\", *)")
  }
}

case class SomeSecret(secret: String) extends NotLoggable {}
