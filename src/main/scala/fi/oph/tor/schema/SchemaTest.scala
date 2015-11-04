package fi.oph.tor.schema

import fi.oph.tor.json.Json
import org.json4s.JsonAST.JObject
import org.json4s.reflect.TypeInfo
import org.json4s._

object SchemaTest extends App {
  import Json._
  val oppija = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpintoOikeus(
        Some(983498343),
        None, None, None,
        Organisaatio("1.2.246.562.10.346830761110"), Organisaatio("1.2.246.562.10.52251087186"), Some(Organisaatio("1.2.246.562.10.42456023292")),
        Suoritus(
          Koulutustoteutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4),
            Some("39/011/2014")
          ),
          None,
          Suoritustapa(
            KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)
          ),
          KoodistoKoodiViite("kesken", Some("Kesken"), "suorituksentila", 1),
          None,
          None,
          None,
          None
        ),
        None,
        None,
        None,
        None
      )
    )
  )
  println(Json.writePretty(oppija))
}

class KoulutusmoduulitoteutusSerializer extends Serializer[Koulutusmoduulitoteutus] {
  private val TheClass = classOf[Koulutusmoduulitoteutus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduulitoteutus] = {
    case (TypeInfo(TheClass, _), json) => json match {
      case moduuli: JObject if moduuli.values.contains("koulutuskoodi") => moduuli.extract[Koulutustoteutus]
      case moduuli: JObject if moduuli.values.contains("tutkinnonosakoodi") => moduuli.extract[Tutkinnonosatoteutus]
      case moduuli: JObject => throw new RuntimeException("Unknown Koulutusmoduulitoteutus" + json)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}