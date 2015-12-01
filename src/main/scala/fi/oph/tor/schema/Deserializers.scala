package fi.oph.tor.schema

import org.json4s._

object Deserializers {
  val deserializers = List(KoulutusmoduulitoteutusDeserializer, KoulutusmoduuliDeserializer, HenkilöDeserialializer, SuoritustapaDeserializer, JärjestämismuotoDeserializer)
}

trait Deserializer[T] extends Serializer[T] {
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}

object KoulutusmoduulitoteutusDeserializer extends Deserializer[Koulutusmoduulitoteutus] {
  private val TheClass = classOf[Koulutusmoduulitoteutus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduulitoteutus] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "koulutusmoduuli" \ "tunniste" \ "koodistoUri" == JString("koulutus") => moduuli.extract[TutkintoKoulutustoteutus]
        case moduuli: JObject if moduuli \ "koulutusmoduuli" \ "tunniste" \ "koodistoUri" == JString("tutkinnonosat") => moduuli.extract[OpsTutkinnonosatoteutus]
        case moduuli: JObject => moduuli.extract[PaikallinenTutkinnonosatoteutus]
      }
  }
}

object KoulutusmoduuliDeserializer extends Deserializer[Koulutusmoduuli] {
  private val TheClass = classOf[Koulutusmoduuli]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduuli] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("koulutus") => moduuli.extract[TutkintoKoulutus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("tutkinnonosat") => moduuli.extract[OpsTutkinnonosa]
        case moduuli: JObject => moduuli.extract[PaikallinenTutkinnonosa]
      }
  }
}

object HenkilöDeserialializer extends Deserializer[Henkilö] {
  private val TheClass = classOf[Henkilö]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Henkilö] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case henkilö: JObject if hasOid(henkilö) && hasHetu(henkilö) => henkilö.extract[FullHenkilö]
        case henkilö: JObject if hasOid(henkilö) => henkilö.extract[OidHenkilö]
        case henkilö => henkilö.extract[NewHenkilö]
      }
  }

  private def hasOid(henkilö: JObject): Boolean = henkilö.values.contains("oid")
  private def hasHetu(henkilö: JObject): Boolean = henkilö.values.contains("hetu")
}

object SuoritustapaDeserializer extends Deserializer[Suoritustapa] {
  private val TheClass = classOf[Suoritustapa]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Suoritustapa] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case suoritustapa: JObject if suoritustapa.values.contains("näyttö") => suoritustapa.extract[NäytöllinenSuoritustapa]
        case suoritustapa => suoritustapa.extract[DefaultSuoritustapa]
      }
  }
}

object JärjestämismuotoDeserializer extends Deserializer[Järjestämismuoto] {
  private val TheClass = classOf[Järjestämismuoto]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Järjestämismuoto] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case järjestämismuoto: JObject if järjestämismuoto.values.contains("oppisopimus") => järjestämismuoto.extract[OppisopimuksellinenJärjestämismuoto]
        case järjestämismuoto => järjestämismuoto.extract[DefaultJärjestämismuoto]
      }
  }
}