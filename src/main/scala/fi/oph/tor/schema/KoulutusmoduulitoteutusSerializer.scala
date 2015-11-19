package fi.oph.tor.schema

import org.json4s._

class KoulutusmoduulitoteutusSerializer extends Serializer[Koulutusmoduulitoteutus] {
  private val TheClass = classOf[Koulutusmoduulitoteutus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduulitoteutus] = {
    case (TypeInfo(TheClass, _), json) => json match {
      case moduuli: JObject if moduuli.values.contains("koulutuskoodi") => moduuli.extract[Koulutustoteutus]
      case moduuli: JObject if moduuli.values.contains("tutkinnonosakoodi") => moduuli.extract[OpsTutkinnonosatoteutus]
      case moduuli: JObject => throw new RuntimeException("Unknown Koulutusmoduulitoteutus" + json)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}
