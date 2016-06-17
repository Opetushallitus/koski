package fi.oph.koski.schema

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.ContextualExtractor
import fi.oph.koski.localization.{English, Finnish, LocalizedString, Swedish}
import org.json4s._
import org.json4s.reflect.{Reflector, TypeInfo}

object Deserializers {
  val deserializers = List(
    ArviointiSerializer,
    KorkeakoulunArviointiDeserializer,
    PerusopetuksenOppiaineenArviointiDeserializer,
    LocalizedStringDeserializer,
    OpiskeluOikeusDeserializer,
    KorkeakouluSuoritusDeserializer,
    KoulutusmoduuliDeserializer,
    HenkilöDeserialializer,
    JärjestämismuotoDeserializer,
    OrganisaatioDeserializer,
    LukionOppiaineDeserializer,
    LukionPäätasonSuoritusDeserializer,
    PerusopetuksenOppiaineDeserializer,
    PerusopetuksenPäätasonSuoritusDeserializer,
    OppiaineenTaiToimintaAlueenSuoritusDeserializer,
    LukionKurssiDeserializer,
    LukioonValmistavanKoulutuksenOsasuoritusDeserializer
  )
}

object ArviointiSerializer extends Serializer[Arviointi] {
  // Threadlocal flags used to prevent recursion. Yes, it's ugly.
  val deserializing = new java.lang.ThreadLocal[Boolean]
  val serializing = new java.lang.ThreadLocal[Boolean]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Arviointi] = {
    case (TypeInfo(c, _), json: JObject) if !deserializing.get && classOf[Arviointi].isAssignableFrom(c) =>
      deserializing.set(true)
      try {
        val arviointi = Extraction.extract(json, Reflector.scalaTypeOf(c)).asInstanceOf[Arviointi]
        (json \\ "hyväksytty") match {
          case JBool(jsonHyväksytty) if (jsonHyväksytty != arviointi.hyväksytty) =>
            ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.validation.arviointi.vääräHyväksyttyArvo())
          case _ =>
            arviointi
        }
      } finally {
        deserializing.set(false)
      }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (a: Arviointi) if (!serializing.get()) =>
      serializing.set(true)
      try {
        val json = Extraction.decompose(a).asInstanceOf[JObject]
        if (!json.values.contains("hyväksytty")) {
          json.merge(JObject("hyväksytty" -> JBool(a.hyväksytty)))
        } else {
          json
        }
      } finally {
        serializing.set(false)
      }
  }
}

object KorkeakoulunArviointiDeserializer extends Deserializer[KorkeakoulunArviointi] {
  private val ArviointiClass = classOf[KorkeakoulunArviointi]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), KorkeakoulunArviointi] = {
    case (TypeInfo(ArviointiClass, _), json) =>
      json match {
        case arviointi: JObject if arviointi \ "arvosana" \ "koodistoUri" == JString("virtaarvosana") => arviointi.extract[KorkeakoulunKoodistostaLöytyväArviointi]
        case arviointi: JObject => arviointi.extract[KorkeakoulunPaikallinenArviointi]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object PerusopetuksenOppiaineenArviointiDeserializer extends Deserializer[PerusopetuksenOppiaineenArviointi] {
  private val PerusopetuksenOppiaineenArviointiClass = classOf[PerusopetuksenOppiaineenArviointi]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PerusopetuksenOppiaineenArviointi] = {
    case (TypeInfo(PerusopetuksenOppiaineenArviointiClass, _), json) =>
      json match {
        case arviointi: JObject if (List(JString("S"), JString("H")).contains(arviointi \ "arvosana" \ "koodiarvo")) => arviointi.extract[SanallinenPerusopetuksenOppiaineenArviointi]
        case arviointi: JObject => arviointi.extract[NumeerinenPerusopetuksenOppiaineenArviointi]
      }
  }
}

trait Deserializer[T] extends Serializer[T] {
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}

object OpiskeluOikeusDeserializer extends Deserializer[Opiskeluoikeus] {
  private val OpiskeluOikeusClass = classOf[Opiskeluoikeus]
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Opiskeluoikeus] = {
    case (TypeInfo(OpiskeluOikeusClass, _), json) =>
      json match {
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ammatillinenkoulutus") => oo.extract[AmmatillinenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("valma") => oo.extract[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("telma") => oo.extract[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetus") => oo.extract[PerusopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenlisaopetus") => oo.extract[PerusopetuksenLisäopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("luva") => oo.extract[LukioonValmistavanKoulutuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("lukiokoulutus") => oo.extract[LukionOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("korkeakoulutus") => oo.extract[KorkeakoulunOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ylioppilastutkinto") => oo.extract[YlioppilastutkinnonOpiskeluoikeus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object KorkeakouluSuoritusDeserializer extends Deserializer[KorkeakouluSuoritus] {
  private val KorkeakouluSuoritusClass = classOf[KorkeakouluSuoritus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), KorkeakouluSuoritus] = {
    case (TypeInfo(KorkeakouluSuoritusClass, _), json) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("korkeakoulututkinto") => suoritus.extract[KorkeakouluTutkinnonSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("korkeakoulunopintojakso") => suoritus.extract[KorkeakoulunOpintojaksonSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object LukionOppiaineDeserializer extends Deserializer[LukionOppiaine] {
  private val LukionOppiaineClass = classOf[LukionOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LukionOppiaine] = {
    case (TypeInfo(LukionOppiaineClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("AI") => moduuli.extract[AidinkieliJaKirjallisuus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("KT") => moduuli.extract[Uskonto]
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[VierasTaiToinenKotimainenKieli]
        case moduuli: JObject if (moduuli \ "oppimäärä").isInstanceOf[JObject] => moduuli.extract[LukionMatematiikka]
        case moduuli: JObject => moduuli.extract[MuuOppiaine]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object LukioonValmistavanKoulutuksenOsasuoritusDeserializer extends Deserializer[LukioonValmistavanKoulutuksenOsasuoritus] {
  private val LukioonValmistavanKoulutuksenOsasuoritusClass = classOf[LukioonValmistavanKoulutuksenOsasuoritus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LukioonValmistavanKoulutuksenOsasuoritus] = {
    case (TypeInfo(LukioonValmistavanKoulutuksenOsasuoritusClass, _), json) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("lukionkurssi") => suoritus.extract[LukionKurssinSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("luvakurssi") => suoritus.extract[LukioonValmistavanKurssinSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object LukionPäätasonSuoritusDeserializer extends Deserializer[LukionPäätasonSuoritus] {
  private val LukionPäätasonSuoritusClass = classOf[LukionPäätasonSuoritus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LukionPäätasonSuoritus] = {
    case (TypeInfo(LukionPäätasonSuoritusClass, _), json) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("lukionoppimaara") => suoritus.extract[LukionOppimääränSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("lukionoppiaineenoppimaara") => suoritus.extract[LukionOppiaineenOppimääränSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}


object PerusopetuksenOppiaineDeserializer extends Deserializer[PerusopetuksenOppiaine] {
  private val PerusopetuksenOppiaineClass = classOf[PerusopetuksenOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PerusopetuksenOppiaine] = {
    case (TypeInfo(PerusopetuksenOppiaineClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("AI") => moduuli.extract[PeruskoulunAidinkieliJaKirjallisuus]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("KT") => moduuli.extract[PeruskoulunUskonto]
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[PeruskoulunVierasTaiToinenKotimainenKieli]
        case moduuli: JObject => moduuli.extract[MuuPeruskoulunOppiaine]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object PerusopetuksenPäätasonSuoritusDeserializer extends Deserializer[PerusopetuksenPäätasonSuoritus] {
  private val PerusopetuksenPäätasonSuoritusClass = classOf[PerusopetuksenPäätasonSuoritus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PerusopetuksenPäätasonSuoritus] = {
    case (TypeInfo(PerusopetuksenPäätasonSuoritusClass, _), json) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenvuosiluokka") => suoritus.extract[PerusopetuksenVuosiluokanSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenoppiaineenoppimaara") => suoritus.extract[PerusopetuksenOppiaineenOppimääränSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenoppimaara") => suoritus.extract[PerusopetuksenOppimääränSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object OppiaineenTaiToimintaAlueenSuoritusDeserializer extends Deserializer[OppiaineenTaiToimintaAlueenSuoritus] {
  private val OppiaineenTaiToimintaAlueenSuoritusClass = classOf[OppiaineenTaiToimintaAlueenSuoritus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), OppiaineenTaiToimintaAlueenSuoritus] = {
    case (TypeInfo(OppiaineenTaiToimintaAlueenSuoritusClass, _), json) =>
      json match {
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenoppiaine") => suoritus.extract[PerusopetuksenOppiaineenSuoritus]
        case suoritus: JObject if suoritus \ "tyyppi" \ "koodiarvo" == JString("perusopetuksentoimintaalue") => suoritus.extract[PerusopetuksenToimintaAlueenSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object KoulutusmoduuliDeserializer extends Deserializer[Koulutusmoduuli] {
  private val classes = List(classOf[Koulutusmoduuli], classOf[AmmatillisenTutkinnonOsa])

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

case class CannotDeserializeException(deserializer: Deserializer[_], json: JValue) extends RuntimeException(deserializer + " cannot deserialize " + json)