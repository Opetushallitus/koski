package fi.oph.koski.json

import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, KoskiOpiskeluoikeusTable}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.KoskiSchema.skipSyntheticProperties
import fi.oph.koski.schema._
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, KoskiHttpSpec, TestEnvironment}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor, SerializationContext, Serializer}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class SyntheticPropertiesSerializationSpec extends AnyFreeSpec with TestEnvironment with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec with DatabaseTestMethods {

  val oppija = KoskiSpecificMockOppijat.teija

  def putOppija[A](oppija: JValue, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val jsonString = JsonMethods.pretty(oppija)
    val result = put("api/oppija", body = jsonString, headers = headers)(f)
    result
  }

  def makeOppija[T: TypeTag](henkilö: Henkilö, opiskeluOikeudet: List[T]): JValue = JObject(
    "henkilö" -> JsonSerializer.serializeWithRoot(henkilö),
    "opiskeluoikeudet" -> JsonSerializer.serializeWithRoot(opiskeluOikeudet)
  )

  "Opiskeluoikeus" - {
    "Synteettinen kenttä on tallennettu Kosken opiskeluoikeuden json-dataan tietokannassa" in {
      val koskiRow = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).result
      ).head

      koskiRow.data.asInstanceOf[JObject].values should contain ("alkamispäivä", "2019-05-30")
    }

    "Synteettisen kentän arvo evaluoidaan deserialisoitaessa kohdeluokan mukaisesti ja edelleen serialisoituna kirjoittaa uuden arvon" in {
      implicit val deserializationContext: ExtractionContext = ExtractionContext(KoskiSchema.schemaFactory).copy(ignoreUnexpectedProperties = true, validate = false)
      val serializationContext = SerializationContext(KoskiSchema.schemaFactory)

      val koskiRow = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).result
      ).head
      koskiRow.data.asInstanceOf[JObject].values should contain("alkamispäivä", "2019-05-30")

      val res = SchemaValidatingExtractor.extract[AlkamispäiväMuuttunut](koskiRow.data).right.get
      res.alkamispäivä shouldBe Some(LocalDate.of(2019, 5, 31))
      Serializer.serialize(res, serializationContext).asInstanceOf[JObject].values should contain ("alkamispäivä", "2019-05-31")
    }
  }

  "Opiskeluoikeuden historia" - {
    "Synteettisen kentän muutos tallentuu opiskeluoikeuden historian json-diffiin" in {
      val oo = getOpiskeluoikeudet(oppija.oid).head match {
        case aoo: AmmatillinenOpiskeluoikeus => aoo.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2019, 6, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
        )
      }

      putOppija(makeOppija(oppija.toHenkilötiedotJaOid, List(oo))){
        verifyResponseStatusOk()
      }

      val versiot = KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(oo.oid.get)(KoskiSpecificSession.systemUser).get
      versiot.size shouldBe 2
      val patch = versiot.maxBy(_.versionumero)

      patch.muutos.asInstanceOf[JArray].arr should contain (
        JObject(("op", JString("replace")), ("path", JString("/alkamispäivä")), ("value", JString("2019-06-01")))
      )
    }

    "Synteettisten kenttien tallennuksen kytkeminen aiheuttaa muutoksen opiskeluoikeuteen, jossa muuten mikään tieto ei ole muuttunut" in {
      val skippingSerializationContext = SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties)
      resetFixtures()

      val oo = getOpiskeluoikeudet(oppija.oid).head

      // Päivitä opiskeluoikeudelle json jossa ei ole mukana synteettisiä kenttiä
      val jsonEiSynteettisiä = KoskiOpiskeluoikeusTable.serialize(oo, skippingSerializationContext)
      runDbSync(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).map(_.data).update(jsonEiSynteettisiä)
      )

      val koskiRowEiSynteettisiä = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).result
      ).head
      koskiRowEiSynteettisiä.data.asInstanceOf[JObject].values should not contain("alkamispäivä", "2019-05-30")

      val vanhatVersiot = KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(oo.oid.get)(KoskiSpecificSession.systemUser).get
      vanhatVersiot.size shouldBe 1

      // Tallenna opiskeluoikeus uudelleen ilman muutoksia (kun synteettisten kenttien serialisointi on voimassa)
      putOppija(makeOppija(oppija.toHenkilötiedotJaOid, List(oo))) {
        verifyResponseStatusOk()
      }

      // Synteettisen kentän serialisointi aiheuttaa muutoksen opiskeluoikeuteen ja uuden historiaversion syntymisen
      val uudetVersiot = KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOid(oo.oid.get)(KoskiSpecificSession.systemUser).get
      uudetVersiot.size shouldBe 2

      val patch = uudetVersiot.maxBy(_.versionumero)
      patch.muutos should be(
        JArray(List(JObject(("op", JString("add")), ("path", JString("/alkamispäivä")), ("value", JString("2019-05-30")))))
      )

      val koskiRowUpdated = runDbSync(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppija.oid).result
      ).head
      koskiRowUpdated.data.asInstanceOf[JObject].values should contain("alkamispäivä", "2019-05-30")
    }

    "Synteettisen kentän arvo evaluoidaan deserialisoitaessa kohdeluokan mukaisesti ja edelleen serialisoituna kirjoittaa uuden arvon" in {
      implicit val deserializationContext: ExtractionContext = ExtractionContext(KoskiSchema.schemaFactory).copy(ignoreUnexpectedProperties = true, validate = false)
      val serializationContext = SerializationContext(KoskiSchema.schemaFactory)

      resetFixtures()

      val oo = getOpiskeluoikeudet(oppija.oid).head match {
        case aoo: AmmatillinenOpiskeluoikeus => aoo.copy(
          tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(LocalDate.of(2019, 6, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
        )
      }
      putOppija(makeOppija(oppija.toHenkilötiedotJaOid, List(oo))) {
        verifyResponseStatusOk()
      }

      val history = runDbSync(
        KoskiApplicationForTests.historyRepository.findByOpiskeluoikeusOidAction(oo.oid.get, 2)(KoskiSpecificSession.systemUser)
      ).get
      history.version shouldBe 2
      history.asOpiskeluoikeusJson.asInstanceOf[JObject].values should contain("alkamispäivä", "2019-06-01")

      val res = SchemaValidatingExtractor.extract[AlkamispäiväMuuttunut](history.asOpiskeluoikeusJson).right.get
      res.alkamispäivä shouldBe Some(LocalDate.of(2019, 6, 2))
      Serializer.serialize(res, serializationContext).asInstanceOf[JObject].values should contain("alkamispäivä", "2019-06-02")
    }
  }

  "LaajatOppijaHenkilöTiedot - synteettinen kenttä estää serialisoinnin jsoniksi" in {
    an[java.lang.ClassNotFoundException] should be thrownBy Serializer.serialize(oppija, SerializationContext(KoskiSchema.schemaFactory))
    an[java.lang.ClassNotFoundException] should be thrownBy Serializer.serialize(oppija, SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties))
  }
}
