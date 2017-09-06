package fi.oph.koski.schema

import fi.oph.koski.documentation.AmmatillinenPerustutkintoExample
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.perftest.LocalPerfTest
import fi.oph.koski.perftest.LocalPerfTest.TestCase
import fi.oph.scalaschema.{SerializationContext, Serializer}

object SerializationPerfTester extends App {
  val oppija = Oppija(MockOppijat.eero, AmmatillinenPerustutkintoExample.perustutkinto.opiskeluoikeudet)

  //LocalPerfTest.runTest(TestCase("serialize oppija", 100, (n) => Json.write(oppija)))

  implicit val context = SerializationContext(KoskiSchema.schema)
  LocalPerfTest.runTest(TestCase("serialize oppija new", 100, (n) => Serializer.serialize(oppija)))
}