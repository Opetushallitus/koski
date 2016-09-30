package fi.oph.koski.schema

import fi.oph.koski.editor.EditorModelSerializer
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{ContextualExtractor, Json}
import fi.oph.koski.localization.{English, Finnish, LocalizedString, Swedish}
import org.json4s._
import org.json4s.reflect.{Reflector, TypeInfo}

object Deserializers {
  val deserializers = List(
    ArviointiSerializer,
    LocalizedStringDeserializer,
    OpiskeluOikeusDeserializer,
    KoulutusmoduuliDeserializer,
    HenkilöDeserialializer,
    JärjestämismuotoDeserializer,
    OrganisaatioDeserializer,
    LukionOppiaineDeserializer,
    IBOppiaineDeserializer,
    PreIBOppiaineDeserializer,
    PerusopetuksenOppiaineDeserializer,
    LukionKurssiDeserializer,
    PreIBKurssiDeserializer,
    SuoritusDeserializer,
    EditorModelSerializer,
    AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsaDeserializer,
    TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsaDeserializer
  )
}

object SuoritusDeserializer extends Deserializer[Suoritus] {

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Suoritus] = {
    case (TypeInfo(c, _), json: JObject) if classOf[Suoritus].isAssignableFrom(c) && c.isInterface =>
      json match {
        case suoritus: JObject if tyyppi(suoritus) == JString("ammatillinentutkinto") => suoritus.extract[AmmatillisenTutkinnonSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("ammatillinentutkintoosittainen") => suoritus.extract[AmmatillisenTutkinnonOsittainenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("ammatillisentutkinnonosa") => suoritus.extract[AmmatillisenTutkinnonOsanSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("nayttotutkintoonvalmistavakoulutus") => suoritus.extract[NäyttötutkintoonValmistavanKoulutuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("nayttotutkintoonvalmistavankoulutuksenosa") => suoritus.extract[NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("esiopetuksensuoritus") => suoritus.extract[EsiopetuksenSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenoppimaara") => suoritus.extract[PerusopetuksenOppimääränSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenoppiaine") => suoritus.extract[PerusopetuksenOppiaineenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksentoimintaalue") => suoritus.extract[PerusopetuksenToiminta_AlueenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenoppiaineenoppimaara") => suoritus.extract[PerusopetuksenOppiaineenOppimääränSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenvuosiluokka") => suoritus.extract[PerusopetuksenVuosiluokanSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetukseenvalmistavaopetus") => suoritus.extract[PerusopetukseenValmistavanOpetuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetukseenvalmistavanopetuksenoppiaine") => suoritus.extract[PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("lukionoppimaara") => suoritus.extract[LukionOppimääränSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("lukionoppiaineenoppimaara") => suoritus.extract[LukionOppiaineenOppimääränSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("lukionoppiaine") => suoritus.extract[LukionOppiaineenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("lukionkurssi") => suoritus.extract[LukionKurssinSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("ibtutkinto") => suoritus.extract[IBTutkinnonSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("preiboppimaara") => suoritus.extract[PreIBSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("iboppiaine") => suoritus.extract[IBOppiaineenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("iboppiainecas") => suoritus.extract[IBCASSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("iboppiaineee") => suoritus.extract[IBExtendedEssaySuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("iboppiainetok") => suoritus.extract[IBTheoryOfKnowledgeSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("preiboppiaine") => suoritus.extract[PreIBOppiaineenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("ibkurssi") => suoritus.extract[IBKurssinSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("preibkurssi") => suoritus.extract[PreIBKurssinSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("ylioppilastutkinto") => suoritus.extract[YlioppilastutkinnonSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("ylioppilastutkinnonkoe") => suoritus.extract[YlioppilastutkinnonKokeenSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("korkeakoulututkinto") => suoritus.extract[KorkeakoulututkinnonSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("korkeakoulunopintojakso") => suoritus.extract[KorkeakoulunOpintojaksonSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("telma") => suoritus.extract[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("telmakoulutuksenosa") => suoritus.extract[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("valma") => suoritus.extract[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("valmakoulutuksenosa") => suoritus.extract[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("luva") => suoritus.extract[LukioonValmistavanKoulutuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("luvakurssi") => suoritus.extract[LukioonValmistavanKurssinSuoritus]

        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenlisaopetus") => suoritus.extract[PerusopetuksenLisäopetuksenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenlisaopetuksenoppiaine") => suoritus.extract[PerusopetuksenLisäopetuksenOppiaineenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("perusopetuksenlisaopetuksentoimintaalue") => suoritus.extract[PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus]
        case suoritus: JObject if tyyppi(suoritus) == JString("muuperusopetuksenlisaopetuksensuoritus") => suoritus.extract[MuuPerusopetuksenLisäopetuksenSuoritus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }

  private def tyyppi(suoritus: JObject) = {
    suoritus \ "tyyppi" \ "koodiarvo"
  }
}

object ArviointiSerializer extends Serializer[Arviointi] {
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

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Arviointi] = {
    case (TypeInfo(c, _), json: JObject) if classOf[Arviointi].isAssignableFrom(c) =>
      val arviointi = Extraction.extract(json, Reflector.scalaTypeOf(c))(format - ArviointiSerializer + KorkeakoulunArviointiDeserializer + PerusopetuksenOppiaineenArviointiDeserializer).asInstanceOf[Arviointi]
      (json \\ "hyväksytty") match {
        case JBool(jsonHyväksytty) if (jsonHyväksytty != arviointi.hyväksytty) =>
          ContextualExtractor.extractionError(KoskiErrorCategory.badRequest.validation.arviointi.vääräHyväksyttyArvo())
        case _ =>
          arviointi
      }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (a: Arviointi) =>
      val json = Extraction.decompose(a)(format - ArviointiSerializer).asInstanceOf[JObject]
      if (!json.values.contains("hyväksytty")) {
        json.merge(JObject("hyväksytty" -> JBool(a.hyväksytty)))
      } else {
        json
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
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("esiopetus") => oo.extract[EsiopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetus") => oo.extract[PerusopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetuksenlisaopetus") => oo.extract[PerusopetuksenLisäopetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("perusopetukseenvalmistavaopetus") => oo.extract[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("luva") => oo.extract[LukioonValmistavanKoulutuksenOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("lukiokoulutus") => oo.extract[LukionOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ibtutkinto") => oo.extract[IBOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("korkeakoulutus") => oo.extract[KorkeakoulunOpiskeluoikeus]
        case oo: JObject if oo \ "tyyppi" \ "koodiarvo" == JString("ylioppilastutkinto") => oo.extract[YlioppilastutkinnonOpiskeluoikeus]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsaDeserializer extends Deserializer[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa] {
  private val AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsaClass = classOf[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa] = {
    case (TypeInfo(AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsaClass, _), json) =>
      json match {
        case moduuli: JObject if (moduuli \ "tunniste" \ "koodiarvo").isInstanceOf[JObject] => moduuli.extract[ValtakunnallinenTutkinnonOsa]
        case moduuli: JObject => moduuli.extract[PaikallinenAmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa]
      }
  }
}

object TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsaDeserializer extends Deserializer[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa] {
  private val TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsaClass = classOf[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa] = {
    case (TypeInfo(TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsaClass, _), json) =>
      json match {
        case moduuli: JObject if (moduuli \ "tunniste" \ "koodiarvo").isInstanceOf[JObject] => moduuli.extract[ValtakunnallinenTutkinnonOsa]
        case moduuli: JObject => moduuli.extract[PaikallinenTyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa]
      }
  }
}

object LukionOppiaineDeserializer extends Deserializer[LukionOppiaine] {
  private val LukionOppiaineClass = classOf[LukionOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LukionOppiaine] = {
    case (TypeInfo(LukionOppiaineClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodiarvo" == JString("AI") => moduuli.extract[AidinkieliJaKirjallisuus]
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[VierasTaiToinenKotimainenKieli]
        case moduuli: JObject if (moduuli \ "oppimäärä").isInstanceOf[JObject] => moduuli.extract[LukionMatematiikka]
        case moduuli: JObject => moduuli.extract[MuuOppiaine]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object PreIBOppiaineDeserializer extends Deserializer[PreIBOppiaine] {
  private val PreIBOppiaineClass = classOf[PreIBOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PreIBOppiaine] = {
    case (TypeInfo(PreIBOppiaineClass, _), json) =>
      json match {
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("oppiaineetib") => moduuli.extract[IBAineRyhmäOppiaine]
        case moduuli: JObject => moduuli.extract[LukionOppiaine]
        case _ => throw CannotDeserializeException(this, json)
      }
  }
}

object IBOppiaineDeserializer extends Deserializer[IBAineRyhmäOppiaine] {
  private val IBOppiaineClass = classOf[IBAineRyhmäOppiaine]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), IBAineRyhmäOppiaine] = {
    case (TypeInfo(IBOppiaineClass, _), json) =>
      json match {
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[IBOppiaineLanguage]
        case moduuli: JObject if (moduuli \ "taso").isInstanceOf[JObject] => moduuli.extract[IBOppiaineMuu]
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
        case moduuli: JObject if (moduuli \ "kieli").isInstanceOf[JObject] => moduuli.extract[PeruskoulunVierasTaiToinenKotimainenKieli]
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("koskioppiaineetyleissivistava") => moduuli.extract[MuuPeruskoulunOppiaine]
        case moduuli: JObject => moduuli.extract[PerusopetuksenPaikallinenValinnainenOppiaine]
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
        case moduuli: JObject if moduuli \ "tunniste" \ "koodistoUri" == JString("tutkinnonosat") => moduuli.extract[ValtakunnallinenTutkinnonOsa]
        case moduuli: JObject => moduuli.extract[PaikallinenTutkinnonOsa]
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

object PreIBKurssiDeserializer extends Deserializer[PreIBKurssi] {
  private val TheClass = classOf[PreIBKurssi]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), PreIBKurssi] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case kurssi: JObject if kurssi \ "tunniste" \ "koodistoUri" == JString("ibkurssit") => kurssi.extract[IBKurssi]
        case kurssi: JObject => kurssi.extract[LukionKurssi]
      }
  }
}

object HenkilöDeserialializer extends Deserializer[Henkilö] {
  private val TheClass = classOf[Henkilö]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Henkilö] = {
    case (TypeInfo(TheClass, _), json) =>
      json match {
        case henkilö: JObject if hasOid(henkilö) && hasHetu(henkilö) => henkilö.extract[TäydellisetHenkilötiedot]
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
        case järjestämismuoto: JObject => järjestämismuoto.extract[JärjestämismuotoIlmanLisätietoja]
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

case class CannotDeserializeException(deserializer: Deserializer[_], json: JValue) extends RuntimeException(deserializer + " cannot deserialize " + Json.write(json))