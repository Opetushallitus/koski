package fi.oph.koski.typemodel

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{KoodistoEnumModelBuilder, LocalizedHtml}
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport

class TypeModelServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
  with RequiresVirkailijaOrPalvelukäyttäjä
  with ContentEncodingSupport
  with NoCache
{
  get("/constraints/:schemaClass") {
    val className = s"fi.oph.koski.schema.${params("schemaClass").replaceAll("[^\\w\\däÄöÖ]", "")}"
    val typeAndSubtypes = TypeExport.toTypeDef(Class.forName(className))
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
