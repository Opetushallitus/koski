package fi.oph.koski.documentation

import java.net.URLEncoder
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._
import org.json4s.jackson.JsonMethods

import scala.Function.const
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.xml.{Elem, Node}
import fi.oph.koski.xml.NodeSeqImplicits._


object KoskiSchemaDocumentHtml {
  def mainSchema = KoskiSchema.schema
  def html(shallowEntities: ClassSchema => Boolean = const(false), focusEntities: ClassSchema => Boolean = const(false), expandEntities: ClassSchema => Boolean = const(true), lang: String, nonce: String)(implicit rootSchema: ClassSchema = mainSchema) = {
    val backlog: List[BacklogItem] = buildBacklog(rootSchema, Some(Nil), Nil, new ArrayBuffer[BacklogItem], shallowEntities, focusEntities, expandEntities).toList
      .sortBy(-_.breadcrumbs.toList.length) // Nones last

    val focusSchema = backlog.map(_.schema).find(focusEntities)
    val title = "Koski-tietomalli" + focusSchema.map(s => " - " + s.title).mkString

    <html lang={lang}>
      <head>
        <title>{title}</title>
        <link nonce={nonce} type="text/css" rel="stylesheet" href="/koski/css/schema-printable.css"/>
      </head>
      <body>
        <h1>{title}</h1>
        {
          backlogHtml(backlog, shallowEntities)
        }
      </body>
    </html>
  }

  private def buildBacklog(x: ClassSchema, breadcrumbs: Option[List[Breadcrumb]], path: List[String], backlog: ArrayBuffer[BacklogItem], shallowEntities: ClassSchema => Boolean, focusEntities: ClassSchema => Boolean, expandEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema): ArrayBuffer[BacklogItem] = {
    val item = BacklogItem(x, breadcrumbs, path)
    val index = backlog.indexWhere(_.schema == item.schema)
    if (index < 0) {
      backlog += item
      if (!shallowEntities(x)) {
        resolveSchemas(x)
          .filter(child => focusEntities(child.schema) || expandEntities(child.schema))
          .foreach { child =>
            val childBreadcrumbs = breadcrumbs.map(_ ++ List(child.breadcrumb))
            val childPath = path :+ child.breadcrumb.property.key
            buildBacklog(child.schema, childBreadcrumbs, childPath, backlog, shallowEntities, const(false), const(true))
          }
      }
    } else if (backlog(index).breadcrumbs.nonEmpty) {
      // remove breadcrumb from this one, because it's contained in multiple contexts
      backlog += backlog.remove(index).copy(breadcrumbs = None)
    }
    backlog
  }

  case class Breadcrumb(schema: ClassSchema, property: Property)
  private case class BacklogItem(schema: ClassSchema, breadcrumbs: Option[List[Breadcrumb]], path: List[String])
  private case class ResolvedSchema(schema: ClassSchema, breadcrumb: Breadcrumb)

  private def anchorsFor(backlog: List[BacklogItem]): Map[ClassSchema, String] = {
    // The same Scala class can produce multiple ClassSchema variants when path-specific
    // annotations include different properties depending on how the class is reached.
    val classNamesWithVariants = backlog
      .groupBy(_.schema.fullClassName)
      .collect {
        case (className, items) if items.map(_.schema).distinct.size > 1 => className
      }.toSet

    // Keep the first anchor stable for old links; disambiguate later variants with the schema path.
    backlog.foldLeft((Set.empty[String], Map.empty[ClassSchema, String])) { case ((seenClassNames, classSchemaToAnchor), item) =>
      val className = item.schema.fullClassName
      val anchor = if (!classNamesWithVariants(className) || !seenClassNames(className)) {
        item.schema.simpleName
      } else {
        val pathAnchorPrefix = item.path match {
          case Nil => "variant"
          case path => path.mkString("-")
        }
        s"$pathAnchorPrefix-${item.schema.simpleName}"
      }

      (seenClassNames + className, classSchemaToAnchor + (item.schema -> anchor))
    }._2
  }

  private def anchorFor(schema: ClassSchema, classSchemaToAnchor: Map[ClassSchema, String]): String =
    classSchemaToAnchor.getOrElse(schema, schema.simpleName)

  private def classSchemasIn(schema: Schema)(implicit rootSchema: ClassSchema): List[ClassSchema] = schema match {
    case s: ClassSchema => List(s)
    case s: AnyOfSchema => s.alternatives.map {
      case s: ClassSchema => s
      case s: ClassRefSchema => resolveSchema(s).asInstanceOf[ClassSchema]
      case _ => ???
    }
    case _ => Nil
  }

  private def backlogHtml(backlog: List[BacklogItem], shallowEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema): List[Elem] = {
    val classSchemaToAnchor = anchorsFor(backlog)
    backlog.map(item => classHtml(item, classSchemaToAnchor, shallowEntities))
  }

  // item is the exact schema section to render, including path-specific schema variants and optional breadcrumbs.
  // classSchemaToAnchor contains every schema variant rendered in this document and maps each to its HTML id.
  // shallowEntities controls whether links to non-expanded schemas point to another generated entity page.
  private def classHtml(item: BacklogItem, classSchemaToAnchor: Map[ClassSchema, String], shallowEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema) = <div class="entity">
    <h3 id={anchorFor(item.schema, classSchemaToAnchor)}>{item.breadcrumbs.toList.flatten.map(bc => <span class="breadcrum"><a href={"#" + urlEncode(anchorFor(bc.schema, classSchemaToAnchor))}>{bc.schema.title}</a> &gt; </span>)}{item.schema.title}</h3>
    {descriptionHtml(item.schema)}
    <table>
      <thead>
        <tr>
          <th class="nimi">Nimi</th>
          <th class="lukumäärä">Lukumäärä</th>
          <th class="tyyppi">Tyyppi</th>
          <th class="kuvaus">Kuvaus</th>
        </tr>
      </thead>
      <tbody>
        {
          item.schema.properties.map { p =>
            val (itemSchema, cardinality) = cardinalityAndItemSchema(p.schema, p.metadata)
            val resolvedItemSchema = resolveSchema(itemSchema)
            val metadatas = p.metadata ++ p.schema.metadata
            <tr>
              <td class="nimi">{p.key}
                {deprecatedHtml(metadatas)}
              </td>

              <td class="lukumäärä">{cardinality}</td>
              <td class="tyyppi">
                {schemaTypeHtml(item.schema, resolvedItemSchema, classSchemaToAnchor, shallowEntities)}
                {metadataHtml(metadatas)}
              </td>
              <td class="kuvaus">
                {descriptionHtml(p)}
              </td>
            </tr>
          }
        }
      </tbody>
    </table>
  </div>

  private def urlEncode(s: String) = URLEncoder.encode(s, "UTF-8")

  private def schemaTypeHtml(parentSchema: ClassSchema, itemSchema: Schema, classSchemaToAnchor: Map[ClassSchema, String], shallowEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema): Elem = itemSchema match {
    case s: ClassSchema => <a href={(if (classSchemaToAnchor.contains(s)) {""} else { "?entity=" + urlEncode(getEntity(parentSchema, s, shallowEntities)) }) + "#" + urlEncode(anchorFor(s, classSchemaToAnchor))}>{s.title}</a>
    case s: AnyOfSchema => <span class={"alternatives " + s.simpleName}>{s.alternatives.map(a => schemaTypeHtml(parentSchema, resolveSchema(a), classSchemaToAnchor, shallowEntities))}</span>
    case s: StringSchema => <span>merkkijono</span> // TODO: schemarajoitukset annotaatioista jne
    case s: NumberSchema => <span>numero</span>
    case s: BooleanSchema => <span>true/false</span>
    case s: DateSchema => <span>päivämäärä</span>
    case _ => ???
  }

  private def resolveSchema(schema: Schema)(implicit rootSchema: ClassSchema): Schema = schema match {
    case s: ClassRefSchema => s.resolve(KoskiSchema.schemaFactory, rootSchema)
    case _ => schema
  }

  private def resolveSchemas(x: ClassSchema)(implicit rootSchema: ClassSchema): Seq[ResolvedSchema] = x.properties.flatMap { p =>
    val (itemSchema, _) = cardinalityAndItemSchema(p.schema, p.metadata)
    val resolvedItemSchema: Schema = resolveSchema(itemSchema)
    classSchemasIn(resolvedItemSchema).map(s => ResolvedSchema(s, Breadcrumb(x, p)))
  }

  private def cardinalityAndItemSchema(s: Schema, metadata: List[Metadata]):(ElementSchema, Cardinality) = s match {
    case s@ListSchema(itemSchema) => (itemSchema.asInstanceOf[ElementSchema], Cardinality(minItems(s, metadata), maxItems(s, metadata)))
    case OptionalSchema(i: ListSchema) =>
      val (itemSchema, Cardinality(min, max)) = cardinalityAndItemSchema(i, metadata)
      (itemSchema, Cardinality(0, max))
    case OptionalSchema(itemSchema: ElementSchema) =>
      (itemSchema, Cardinality(0, Some(1)))
    case s: ElementSchema => (s, Cardinality(1, Some(1)))
    case _ => ???
  }

  private def minItems(s: ListSchema, metadata: List[Metadata]): Int = (metadata ++ s.metadata).collect {
    case MinItems(min) => min
  }.headOption.getOrElse(0)

  private def maxItems(s: ListSchema, metadata: List[Metadata]): Option[Int] = (metadata ++ s.metadata).collect {
    case MaxItems(max) => max
  }.headOption

  private def metadataHtml(metadatas: List[Metadata]) = {
    {
      metadatas.flatMap {
        case k: KoodistoUri =>Some(<div class="koodisto">Koodisto: {k.asLink}</div>)
        case k: KoodistoKoodiarvo =>Some(<div class="koodiarvo">Hyväksytty koodiarvo: {k.arvo}</div>)
        case o: OksaUri => Some(<div class="oksa">Oksa: {o.asLink}</div>)
        case _ => None
      }
    }
  }

  private def descriptionHtml(p: Property): List[Elem] = descriptionHtml(p.metadata.reverse ++ p.schema.metadata)
  private def descriptionHtml(p: ObjectWithMetadata[_]): List[Elem] = descriptionHtml(p.metadata)

  private def descriptionHtml(metadata: List[Metadata]): List[Elem] = (metadata flatMap {
    case Description(desc) => Some(<span class="description">{formatDescription(desc)}</span>)
    case ReadOnly(desc) => Some(<div class="readonly">{formatDescription(desc)}</div>)
    case _ => None
  }) ++ onlyWhenHtml(metadata) ++ sensitiveDataHtml(metadata) ++ deprecatedHtml(metadata, includeMessage = true) ++ redundantDataHtml(metadata)

  private def onlyWhenHtml(metadata: List[Metadata]): List[Elem] = metadata.collect { case o: OnlyWhen => o } match {
    case Nil => Nil
    case conditions => List(<div class="onlywhen">Vain kun { intersperse(<span>tai</span>, conditions.map(c => <code>{c.path}={JsonMethods.compact(c.serializableForm.value)}</code>)) }</div>)
  }

  private def sensitiveDataHtml(metadata: List[Metadata]): List[Elem] = metadata.collect {
    case s: SensitiveData => <div class="sensitive">Erityinen henkilötieto + salassa pidettävä tieto.</div>
  }


  private def deprecatedHtml(metadata: List[Metadata], includeMessage: Boolean = false): List[Elem] = metadata.collect {
    case d: Deprecated =>
      <div class="deprecated">{
        if (includeMessage) {
          <span>Vanhentunut kenttä: </span>
          <span class="deprecated__message">{d.msg}</span>
        } else {
          "Vanhentunut kenttä"
        }}
      </div>
  }

  private def redundantDataHtml(metadata: List[Metadata]): List[Elem] = metadata.collect {
    case s: RedundantData => <div class="redundant">Kenttä ei ole käytössä. Koski ei ota vastaan kentässä siirrettyä tietoa.</div>
  }

  def intersperse[E](x: E, xs:Seq[E]): Seq[E] = (x, xs) match {
    case (_, Nil)     => Nil
    case (_, Seq(x))  => Seq(x)
    case (sep, y::ys) => y+:sep+:intersperse(sep, ys)
  }

  case class Cardinality(min: Int, max: Option[Int]) {
    override def toString: String = (min, max) match {
      case (1, Some(1)) => "1"
      case (min, Some(max)) => s"$min..$max"
      case (min, None) => s"$min..n"
    }
  }

  private def formatDescription(s: String): Array[Node] = {
    val v = if (s.endsWith(".")) { s } else { s + "." }
    v.split("\n").map(Markdown.markdownToXhtml)
  }

  private val cachedEntities: collection.mutable.Map[ClassSchema, Option[String]] = collection.mutable.Map.empty
  private def getEntity(parentSchema: ClassSchema, schema: ClassSchema, shallowEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema) = if (shallowEntities(parentSchema)) {
    synchronized {
      cachedEntities.getOrElseUpdate(schema, OpiskeluoikeusSchemaFinder(schema, shallowEntities).findOpiskeluoikeusSchema.map(_.simpleName)).getOrElse(schema.simpleName)
    }
  } else {
    schema.simpleName
  }

  case class OpiskeluoikeusSchemaFinder(itemSchema: ClassSchema, shallowEntities: ClassSchema => Boolean)(implicit rootSchema: ClassSchema) {
    def findOpiskeluoikeusSchema: Option[ClassSchema] =
      opiskeluoikeusSchemas.find(ooSchema => containsItem(nonShallowItemsFrom(ooSchema)))

    private def nonShallowItemsFrom(s: ClassSchema): Seq[ClassSchema] = resolveSchemas(s).map(_.schema)
      .filterNot(shallowEntities)

    @tailrec private def containsItem(schemas: Seq[ClassSchema], alreadySearched: List[ClassSchema] = Nil): Boolean = {
      val filteredSchemas = schemas.filterNot(alreadySearched.contains)
      if (filteredSchemas.isEmpty) {
        false
      } else {
        filteredSchemas.exists(_.fullClassName == itemSchema.fullClassName) ||
          containsItem(filteredSchemas.flatMap(nonShallowItemsFrom), alreadySearched ++ filteredSchemas)
      }
    }
  }

  private lazy val opiskeluoikeusSchemas = resolveSchemas(mainSchema)(mainSchema).map(_.schema).filter(isOpiskeluoikeusSchema)
  private def isOpiskeluoikeusSchema(s: ClassSchema) =
    classOf[Opiskeluoikeus].isAssignableFrom(Class.forName(s.fullClassName))
}
