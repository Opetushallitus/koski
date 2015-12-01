package fi.oph.tor.schema

import org.json4s._

class KoulutusmoduulitoteutusSerializer extends Serializer[Koulutusmoduulitoteutus] {
  private val TheClass = classOf[Koulutusmoduulitoteutus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduulitoteutus] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "koulutusmoduuli" \ "tunniste" \ "koodistoUri" == JString("koulutus") => moduuli.extract[TutkintoKoulutustoteutus]
        case moduuli: JObject if moduuli \ "koulutusmoduuli" \ "tunniste" \ "koodistoUri" == JString("tutkinnonosat") => moduuli.extract[OpsTutkinnonosatoteutus]
        case moduuli: JObject => moduuli.extract[PaikallinenTutkinnonosatoteutus]
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}