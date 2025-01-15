package fi.oph.koski.typemodel

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EuropeanSchoolOfHelsinkiOppiaineet, KoodistoEnumModelBuilder, LocalizedHtml, NuortenPerusopetusPakollisetOppiaineet}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.koskiuser.{HasKoskiSpecificSession, RequiresSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.schema.{Koodistokoodiviite, Suoritus}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport

class TypeModelServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
  with RequiresSession
  with ContentEncodingSupport
  with NoCache
{
  get("/constraints/:schemaClass") {
    val className = s"fi.oph.koski.schema.${params("schemaClass").replaceAll("[^\\w\\däÄöÖ]", "")}"
    val typeAndSubtypes = TypeExport.toTypeDef(
      clss = Class.forName(className),
      followClassRefs = true,
      stopParsingAt = stopSchemaParsingForConstraintsAt,
    )
    val requestedType = typeAndSubtypes.find(_.fullClassName == className)
    val constraint = requestedType
      .map(t => Constraints.build(t, typeAndSubtypes))
      .getOrElse(AnyConstraint())

    renderObject[Constraint](constraint)
  }

  get[GroupedKoodistot]("/koodisto/:koodistoUri") {
    toGroupedKoodistoValues(getKooditFromRequestParams)
  }

  get("/opiskeluoikeustyypit") {
    opiskeluoikeustyypitToClassnames
  }

  get("/prefill/suoritukset/:koodistoUri/:koodiarvo") {
    val luokkaAstePattern = """(\d)""".r
    val eshLuokkaAstePattern = """^((?:N[1-2])|(?:P[1-5])|(?:S[1-7]))$""".r
    val toimintaAlueittain = params.get("toimintaAlueittain").map(_.toBoolean).getOrElse(false)

    (params("koodistoUri"), params("koodiarvo")) match {
      case ("perusopetuksenluokkaaste", luokkaAstePattern(luokkaAste)) =>
        NuortenPerusopetusPakollisetOppiaineet(application.koodistoViitePalvelu).pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste.toInt, toimintaAlueittain)
      case ("europeanschoolofhelsinkiluokkaaste", eshLuokkaAstePattern(luokkaAste)) =>
        EuropeanSchoolOfHelsinkiOppiaineet(application.koodistoViitePalvelu).eshOsaSuoritukset(luokkaAste)
      case ("koulutus", "201101") =>
        NuortenPerusopetusPakollisetOppiaineet(application.koodistoViitePalvelu).päättötodistuksenSuoritukset(params("tyyppi"), toimintaAlueittain)
      case _ =>
        logger.error(s"Prefill failed for unexpected code ${params("koodistoUri")}/${params("koodiarvo")}")
        haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  private def localization: LocalizedHtml =
    LocalizedHtml.get(session, application.koskiLocalizationRepository)

  private def getKooditFromRequestParams: List[Koodistokoodiviite] =
    koodistojenKoodit(koodistotByString(params("koodistoUri")))

  private def koodistojenKoodit(koodistot: List[KoodistoViite]): List[Koodistokoodiviite] =
    koodistot.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet)

  private def toGroupedKoodistoValues(koodit: List[Koodistokoodiviite]): GroupedKoodistot =
    GroupedKoodistot(
      koodit.groupBy(viite => {
        KoodistoEnumModelBuilder
          .koodistoName(viite)(localization, application.koodistoViitePalvelu)
          .getOrElse(viite.koodistoUri)
      })
    )

  private def koodistotByString(str: String): List[KoodistoViite] = {
    // note: silently omits non-existing koodistot from result
    val koodistoUriParts = str.split(",").toList
    koodistoUriParts flatMap { part: String =>
      application.koodistoViitePalvelu.getLatestVersionOptional(part)
    }
  }

  private lazy val opiskeluoikeustyypitToClassnames: List[OpiskeluoikeusClass] = TypeExport
    .toTypeDef(Class.forName("fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus"))
    .head
    .asInstanceOf[UnionType]
    .anyOf
    .collect { case ClassRef(c) => c }
    .flatMap(OpiskeluoikeusClass.apply)

  // Lisää tähän sellaiset luokat, jotka aiheuttavat valtavien tietomallivastausten luonnin
  private lazy val stopSchemaParsingForConstraintsAt: List[String] = List(
    "fi.oph.koski.schema.OsaamisenTunnustaminen",
  )
}

case class GroupedKoodistot(
  koodistot: Map[String, List[Koodistokoodiviite]],
)

case class OpiskeluoikeusClass(
  className: String,
  tyyppi: String,
  suoritukset: List[SuoritusClass],
  opiskeluoikeusjaksot: List[String],
  lisätiedot: List[String],
)

object OpiskeluoikeusClass {
  def apply(cname: String): List[OpiskeluoikeusClass] = {
    TypeExport
      .getObjectModels(Class.forName(cname))
      .flatMap(o => getTyyppi(o).map((o, _)))
      .map { case (oo, tyyppi) => OpiskeluoikeusClass(
        className = cname,
        tyyppi = tyyppi,
        suoritukset = SuoritusClass(oo),
        opiskeluoikeusjaksot = oo
          .zoom(Seq("tila", "opiskeluoikeusjaksot", "[]"))
          .map(_.asInstanceOf[ObjectType].fullClassName).toList,
        lisätiedot = oo
          .zoom(Seq("lisätiedot"))
          .map(_.asInstanceOf[ObjectType].fullClassName).toList,
      ) }
  }

  def getTyyppi(o: ObjectType): Option[String] =
    (o.properties("tyyppi") match {
      case tyyppi: ObjectType => tyyppi.properties("koodiarvo") match {
        case e: EnumType[_] => e.enumValues.headOption
        case _ => None
      }
      case _ => None
    }).asInstanceOf[Option[String]]
}

case class SuoritusClass(
  className: String,
  tyyppi: String,
)

object SuoritusClass {
  def apply(ooTypeDef: ObjectType): List[SuoritusClass] =
    TypeExport
      .getObjectModels(ooTypeDef.properties("suoritukset"))
      .flatMap(o => OpiskeluoikeusClass.getTyyppi(o).map(SuoritusClass(o.fullClassName, _)))
}
