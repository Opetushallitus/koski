package fi.oph.koski.koodisto

import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, Finnish, SynteettinenKoodiviite}
import fi.oph.scalaschema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}

trait SynteettinenKoodisto {
  def koodistoUri: String
  def dokumentaatio: String
  def validoi(koodiarvo: String): Boolean
}

case class SynteettinenKoodiResolvingCustomDeserializer() extends CustomDeserializer with Logging {

  override def extract(json: JsonCursor, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext) = {
    val viite = SchemaValidatingExtractor.extract(json, schema, metadata)(context.copy(customDeserializers = Nil))
    viite match {
      case Right(viite: SynteettinenKoodiviite) =>
        SynteettinenKoodiResolvingCustomDeserializer.synteettisetKoodistot.find(_.koodistoUri == viite.koodistoUri) match {
          case Some(sk) if sk.validoi(viite.koodiarvo) =>
            Right(fillLokalisaatiot(viite))
          case Some(_) =>
            Left(List(ValidationError(json.path, json.json, OtherViolation("Koodi " + viite.koodiarvo + " ei ole kelvollinen synteettinen koodiarvo koodistossa " + viite.koodistoUri, "tuntematonKoodi"))))
          case _ =>
            Left(List(ValidationError(json.path, json.json, OtherViolation("Tuntematon synteettinen koodistoUri " + viite.koodistoUri, "tuntematonSynteettinenKoodistoUri"))))
        }
      case errors => errors
    }
  }

  private def fillLokalisaatiot(viite: SynteettinenKoodiviite): SynteettinenKoodiviite =
    viite.copy(
      nimi = Some(Finnish(viite.koodiarvo, Some(viite.koodiarvo), Some(viite.koodiarvo))),
      lyhytNimi = Some(Finnish(viite.koodiarvo, Some(viite.koodiarvo), Some(viite.koodiarvo)))
    )

  def isApplicable(schema: SchemaWithClassName): Boolean = schema.appliesToClass(classOf[SynteettinenKoodiviite])
}

object SynteettinenKoodiResolvingCustomDeserializer {
  lazy val synteettisetKoodistot: Seq[SynteettinenKoodisto] = EuropeanSchoolOfHelsinkiOpiskeluoikeus.synteettisetKoodistot
}
