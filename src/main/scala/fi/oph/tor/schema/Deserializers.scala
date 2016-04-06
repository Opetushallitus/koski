package fi.oph.tor.schema

import fi.oph.tor.schema.PeruskoulunOppiaine
import org.json4s._

object Deserializers {
  val deserializers = List(OpiskeluOikeusSerializer, SuoritusDeserializer, KoulutusmoduuliDeserializer, HenkilöDeserialializer, JärjestämismuotoDeserializer, OrganisaatioDeserializer, PeruskoulunOppiaineDeserializer)
}

trait Deserializer[T] extends Serializer[T] {
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}

object OpiskeluOikeusSerializer extends Deserializer[OpiskeluOikeus] {
  private val OpiskeluOikeusClass = classOf[OpiskeluOikeus]
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), OpiskeluOikeus] = {
    case (TypeInfo(OpiskeluOikeusClass, _), json) =>
      json match {
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ammatillinenkoulutus") => oo.extract[AmmatillinenOpiskeluOikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("peruskoulutus") => oo.extract[PeruskouluOpiskeluOikeus]
      }
  }
}

object SuoritusDeserializer extends Deserializer[Suoritus] {
  private val classes = List(classOf[Suoritus], classOf[AmmatillinenTutkinnonosaSuoritus])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Suoritus] = {
    case (TypeInfo(c, _), json) if (classes.contains(c)) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkintosuoritus") => suoritus.extract[AmmatillinenTutkintoSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinenopstutkinnonosasuoritus") => suoritus.extract[AmmatillinenOpsTutkinnonosaSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinenpaikallinentutkinnonosasuoritus") => suoritus.extract[AmmatillinenPaikallinenTutkinnonosaSuoritus]
      }
  }
}

object PeruskoulunOppiaineDeserializer extends Deserializer[PeruskoulunOppiaine] {
  private val TheClass = classOf[PeruskoulunOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PeruskoulunOppiaine] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("AI") => moduuli.extract[AidinkieliJaKirjallisuus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("KT") => moduuli.extract[Uskonto]
        case moduuli: JObject => moduuli.extract[Oppiaine]
      }
  }
}

object KoulutusmoduuliDeserializer extends Deserializer[Koulutusmoduuli] {
  private val TheClass = classOf[Koulutusmoduuli]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduuli] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("koulutus") => moduuli.extract[AmmatillinenTutkintoKoulutus]
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
        case henkilö: JObject => henkilö.extract[NewHenkilö]
      }
  }

  private def hasOid(henkilö: JObject): Boolean = henkilö.values.contains("oid")
  private def hasHetu(henkilö: JObject): Boolean = henkilö.values.contains("hetu")
}

object JärjestämismuotoDeserializer extends Deserializer[Järjestämismuoto] {
  private val TheClass = classOf[Järjestämismuoto]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Järjestämismuoto] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case järjestämismuoto: JObject if järjestämismuoto.values.contains("oppisopimus") => järjestämismuoto.extract[OppisopimuksellinenJärjestämismuoto]
        case järjestämismuoto: JObject => järjestämismuoto.extract[DefaultJärjestämismuoto]
      }
  }
}

object OrganisaatioDeserializer extends Deserializer[Organisaatio] {
  val OrganisaatioClass = classOf[Organisaatio]
  val OrganisaatioWithOidClass = classOf[OrganisaatioWithOid]
  val classes = List(OrganisaatioClass, OrganisaatioWithOidClass)

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Organisaatio] = {
    case (TypeInfo(c, _), json) if (classes.contains(c)) =>
      json match {
        case organisaatio: JObject if organisaatio.values.contains("oppilaitosnumero") => organisaatio.extract[Oppilaitos]
        case organisaatio: JObject if organisaatio.values.contains("tutkintotoimikunnanNumero") => organisaatio.extract[Tutkintotoimikunta]
        case organisaatio: JObject if organisaatio.values.contains("oid") => organisaatio.extract[OidOrganisaatio]
        case organisaatio: JObject => organisaatio.extract[Yritys]
      }
  }
}