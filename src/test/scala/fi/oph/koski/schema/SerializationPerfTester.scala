package fi.oph.koski.schema

import fi.oph.koski.documentation.AmmatillinenPerustutkintoExample
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.perftest.LocalPerfTest
import fi.oph.koski.perftest.LocalPerfTest.TestCase
import fi.oph.scalaschema.{SerializationContext, Serializer}

object SerializationPerfTester extends App {
  val oppija = Oppija(KoskiSpecificMockOppijat.eero.toHenkilötiedotJaOid, AmmatillinenPerustutkintoExample.perustutkinto.opiskeluoikeudet)

  //LocalPerfTest.runTest(TestCase("serialize oppija", 100, (n) => Json.write(oppija)))

  val context = SerializationContext(KoskiSchema.schemaFactory)
  LocalPerfTest.runTest(TestCase("serialize oppija new", 100, (n) => Serializer.serialize(oppija, context)))
}
