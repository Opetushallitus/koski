package fi.oph.koski.massaluovutus

import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema._
import org.json4s.JValue

import scala.annotation.tailrec

// Tulostiedoston skeema dokumentaatioon, johdettu samoista säännöistä kuin serialisointi:
// kentät karsitaan samalla ehdolla kuin serialisoinnin propertyProcessor ja opiskeluoikeuksien unioni
// rajataan annettuihin opiskeluoikeuden tyyppeihin. Saavuttamattomiksi jääneet määritelmät jätetään pois.
case class MassaluovutusResultSchema(
  root: ClassSchema,
  includeProperty: Property => Boolean,
  opiskeluoikeudenTyypit: Option[List[String]] = None,
) {
  lazy val schema: ClassSchema = withoutUnreachableDefinitions(restrict(root).asInstanceOf[ClassSchema])
  lazy val json: JValue = SchemaToJson.toJsonSchema(schema)

  private val opiskeluoikeusUnion = classOf[KoskeenTallennettavaOpiskeluoikeus].getName

  private def restrict(schema: Schema): Schema = schema match {
    case s: ClassSchema => s.copy(
      properties = s.properties.filter(includeProperty).map(p => p.copy(schema = restrict(p.schema))),
      definitions = s.definitions.map(restrict).collect { case d: SchemaWithClassName => d },
    )
    case s: AnyOfSchema =>
      val alternatives = opiskeluoikeudenTyypit match {
        case Some(allowedTypes) if s.fullClassName == opiskeluoikeusUnion => s.alternatives.filter(a => opiskeluoikeudenTyyppi(a).exists(allowedTypes.contains))
        case _ => s.alternatives
      }
      s.copy(alternatives = alternatives.map(restrict).collect { case a: SchemaWithClassName => a })
    case s: OptionalSchema => s.copy(itemSchema = restrict(s.itemSchema))
    case s: ListSchema => s.copy(itemSchema = restrict(s.itemSchema))
    case s: MapSchema => s.copy(itemSchema = restrict(s.itemSchema))
    case other => other
  }

  // Opiskeluoikeusluokan tyyppi luetaan sen tyyppi-kentän KoodistoKoodiarvo-merkinnästä
  private def opiskeluoikeudenTyyppi(alternative: SchemaWithClassName): Option[String] = {
    val classSchema = alternative match {
      case s: ClassSchema => Some(s)
      case s => root.definitions.collectFirst { case d: ClassSchema if d.definitionKey == s.definitionKey => d }
    }
    classSchema.flatMap(_.properties.find(_.key == "tyyppi")).flatMap(_.metadata.collectFirst { case KoodistoKoodiarvo(arvo) => arvo })
  }

  private def withoutUnreachableDefinitions(s: ClassSchema): ClassSchema = {
    val definitionsByKey = s.definitions.map(d => d.definitionKey -> d).toMap
    @tailrec
    def collect(pending: Set[DefinitionKey], found: Set[DefinitionKey]): Set[DefinitionKey] =
      if (pending.isEmpty) found
      else {
        val discovered = pending.flatMap(key => definitionsByKey.get(key).toList.flatMap(references)) -- found
        collect(discovered, found ++ discovered)
      }
    val fromRoot = s.properties.flatMap(p => references(p.schema)).toSet
    val reachable = collect(fromRoot, fromRoot)
    s.copy(definitions = s.definitions.filter(d => reachable(d.definitionKey)))
  }

  private def references(schema: Schema): List[DefinitionKey] = schema match {
    case s: ClassRefSchema => List(s.definitionKey)
    case s: ClassSchema => s.properties.flatMap(p => references(p.schema))
    case s: AnyOfSchema => s.alternatives.flatMap(references)
    case s: OptionalSchema => references(s.itemSchema)
    case s: ListSchema => references(s.itemSchema)
    case s: MapSchema => references(s.itemSchema)
    case s: FlattenedSchema => references(s.classSchema)
    case _ => Nil
  }
}
