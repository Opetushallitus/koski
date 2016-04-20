package fi.oph.tor.schema

import fi.oph.tor.localization.{English, Swedish, Finnish, LocalizedString}
import org.json4s._
import org.json4s.reflect.TypeInfo

object Deserializers {
  val deserializers = List(
    LocalizedStringDeserializer,
    OpiskeluOikeusSerializer,
    SuoritusDeserializer,
    KoulutusmoduuliDeserializer,
    HenkilöDeserialializer,
    JärjestämismuotoDeserializer,
    OrganisaatioDeserializer,
    YleissivistavaOppiaineDeserializer,
    LukionKurssiDeserializer
  )
}

trait Deserializer[T] extends Serializer[T] {
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}

object OpiskeluOikeusSerializer extends Deserializer[Opiskeluoikeus] {
  private val OpiskeluOikeusClass = classOf[Opiskeluoikeus]
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Opiskeluoikeus] = {
    case (TypeInfo(OpiskeluOikeusClass, _), json) =>
      json match {
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ammatillinenkoulutus") => oo.extract[AmmatillinenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetus") => oo.extract[PerusopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("lukiokoulutus") => oo.extract[LukionOpiskeluoikeus]
      }
  }
}

object SuoritusDeserializer extends Deserializer[Suoritus] {
  private val classes = List(classOf[Suoritus])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Suoritus] = {
    case (TypeInfo(c, _), json) if (classes.contains(c)) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkintosuoritus") => suoritus.extract[AmmatillisenTutkinnonSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("ammatillinentutkinnonosasuoritus") => suoritus.extract[AmmatillisenTutkinnonosanSuoritus]
      }
  }
}

object YleissivistavaOppiaineDeserializer extends Deserializer[YleissivistavaOppiaine] {
  private val classes = List(classOf[PerusopetuksenOppiaine], classOf[LukionOppiaine])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), YleissivistavaOppiaine] = {
    case (TypeInfo(c, _), json) if classes.contains(c) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("AI") => moduuli.extract[AidinkieliJaKirjallisuus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("KT") => moduuli.extract[Uskonto]
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[VierasTaiToinenKotimainenKieli]
        case moduuli: JObject if (moduuli \ "oppimäärä").isInstanceOf[JObject] => moduuli.extract[LukionMatematiikka]
        case moduuli: JObject => moduuli.extract[MuuOppiaine]
      }
  }
}

object KoulutusmoduuliDeserializer extends Deserializer[Koulutusmoduuli] {
  private val classes = List(classOf[Koulutusmoduuli], classOf[AmmatillinenTutkinnonOsa])

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduuli] = {
    case (TypeInfo(c, _), json) if classes.contains(c) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("koulutus") => moduuli.extract[AmmatillinenTutkintoKoulutus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("tutkinnonosat") => moduuli.extract[OpsTutkinnonosa]
        case moduuli: JObject => moduuli.extract[PaikallinenTutkinnonosa]
      }
  }
}

object LukionKurssiDeserializer extends Deserializer[LukionKurssi] {
  private val TheClass = classOf[LukionKurssi]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LukionKurssi] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case kurssi: JObject if kurssi \ "tunniste" \ "koodistoUri" == JString("lukionkurssit") => kurssi.extract[ValtakunnallinenLukionKurssi]
        case kurssi: JObject => kurssi.extract[PaikallinenLukionKurssi]
      }
  }
}

object HenkilöDeserialializer extends Deserializer[Henkilö] {
  private val TheClass = classOf[Henkilö]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Henkilö] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case henkilö: JObject if hasOid(henkilö) && hasHetu(henkilö) => henkilö.extract[TaydellisetHenkilötiedot]
        case henkilö: JObject if hasOid(henkilö) => henkilö.extract[OidHenkilö]
        case henkilö: JObject => henkilö.extract[UusiHenkilö]
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

object LocalizedStringDeserializer extends Deserializer[LocalizedString] {
  val LocalizedStringClass = classOf[LocalizedString]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LocalizedString] = {
    case (TypeInfo(LocalizedStringClass, _), json: JObject) if json.values.contains("fi") => json.extract[Finnish]
    case (TypeInfo(LocalizedStringClass, _), json: JObject) if json.values.contains("sv") => json.extract[Swedish]
    case (TypeInfo(LocalizedStringClass, _), json: JObject) if json.values.contains("en") => json.extract[English]
  }
}