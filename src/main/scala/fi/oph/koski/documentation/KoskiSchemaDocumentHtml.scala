package fi.oph.koski.documentation

import fi.oph.koski.html.ContentSecurityPolicy

import java.net.URLEncoder
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.koski.util.Cryptographic
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._
import org.json4s.jackson.JsonMethods

import scala.Function.const
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.xml.{Elem, Node}

object KoskiSchemaDocumentHtml {
  def mainSchema = KoskiSchema.schema
  def html(shallowEntities: ClassSchema => Boolean = const(false), focusEntities: ClassSchema => Boolean = const(false), expandEntities: ClassSchema => Boolean = const(true), lang: String) = {
    val backlog: List[(String, Option[List[Breadcrumb]])] = buildBacklog(mainSchema, Some(Nil), new ArrayBuffer[(String, Option[List[Breadcrumb]])], shallowEntities, focusEntities, expandEntities).toList
      .sortBy(-_._2.toList.length) // Nones last
    val schemaBacklog = backlog.map {
      case (name, breadcrumbs) => (KoskiSchema.schemaFactory.createSchema(name).asInstanceOf[ClassSchema], breadcrumbs)
    }

    val focusSchema = schemaBacklog.map(_._1).find(focusEntities)
    val title = "Koski-tietomalli" + focusSchema.map(s => " - " + s.title).mkString

    val nonce = Cryptographic.nonce

    <html lang={lang}>
      <head>
        {ContentSecurityPolicy.create(nonce)}
        <title>{title}</title>
        <link nonce={nonce} type="text/css" rel="stylesheet" href="/koski/css/schema-printable.css"/>
      </head>
      <body>
        <h1>{title}</h1>
        {
          schemaBacklog.map{case (s, breadcrumbs) =>
            classHtml(s, breadcrumbs, backlog.map(_._1), shallowEntities)
          }
        }
      </body>
    </html>
  }

  private def buildBacklog(x: ClassSchema, breadcrumbs: Option[List[Breadcrumb]], backlog: ArrayBuffer[(String, Option[List[Breadcrumb]])], shallowEntities: ClassSchema => Boolean, focusEntities: ClassSchema => Boolean, expandEntities: ClassSchema => Boolean): ArrayBuffer[(String, Option[List[Breadcrumb]])] = {
    val name = x.fullClassName
    val index = backlog.indexWhere(_._1 == name)
    if (index < 0) {
      backlog +=((name, breadcrumbs))
      if (!shallowEntities(x)) {
        resolveSchemas(x)
          .filter { case (s, _) => focusEntities(s) || expandEntities(s) }
          .foreach { case (s, breadcrumb) =>
            buildBacklog(s, breadcrumbs.map(_ ++ List(breadcrumb)), backlog, shallowEntities, const(false), const(true))
          }
      }
    } else if (backlog(index)._2.nonEmpty) {
      // remove breadcrumb from this one, because it's contained in multiple contexts
      backlog += backlog.remove(index).copy(_2 = None)
    }
    backlog
  }

  case class Breadcrumb(schema: ClassSchema, property: Property)

  private def classSchemasIn(schema: Schema): List[ClassSchema] = schema match {
    case s: ClassSchema => List(s)
    case s: AnyOfSchema => s.alternatives.map {
      case s: ClassSchema => s
      case s: ClassRefSchema => resolveSchema(s).asInstanceOf[ClassSchema]
      case _ => ???
    }
    case _ => Nil
  }

  private def classHtml(schema: ClassSchema, breadcrumbs: Option[List[Breadcrumb]], includedEntities: List[String], shallowEntities: ClassSchema => Boolean) = <div class="entity">
    <h3 id={schema.simpleName}>{breadcrumbs.toList.flatten.map(bc => <span class="breadcrum"><a href={"#" + urlEncode(bc.schema.simpleName)}>{bc.schema.title}</a> &gt; </span>)}{schema.title}</h3>
    {descriptionHtml(schema)}
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
          schema.properties.map { p =>
            val (itemSchema, cardinality) = cardinalityAndItemSchema(p.schema, p.metadata)
            val resolvedItemSchema = resolveSchema(itemSchema)
            val metadatas = p.metadata ++ p.schema.metadata
            <tr>
              <td class="nimi">{p.key}
                {deprecatedHtml(metadatas)}
              </td>

              <td class="lukumäärä">{cardinality}</td>
              <td class="tyyppi">
                {schemaTypeHtml(schema, resolvedItemSchema, includedEntities, shallowEntities)}
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

  private def schemaTypeHtml(parentSchema: ClassSchema, itemSchema: Schema, includedEntities: List[String], shallowEntities: ClassSchema => Boolean): Elem = itemSchema match {
    case s: ClassSchema => <a href={(if (includedEntities.contains(s.fullClassName)) {""} else { "?entity=" + urlEncode(getEntity(parentSchema, s, shallowEntities)) }) + "#" + urlEncode(s.simpleName)}>{s.title}</a>
    case s: AnyOfSchema => <span class={"alternatives " + s.simpleName}>{s.alternatives.map(a => schemaTypeHtml(parentSchema, resolveSchema(a), includedEntities, shallowEntities))}</span>
    case s: StringSchema => <span>merkkijono</span> // TODO: schemarajoitukset annotaatioista jne
    case s: NumberSchema => <span>numero</span>
    case s: BooleanSchema => <span>true/false</span>
    case s: DateSchema => <span>päivämäärä</span>
    case _ => ???
  }

  private def resolveSchema(schema: Schema): Schema = schema match {
    case s: ClassRefSchema => s.resolve(KoskiSchema.schemaFactory)
    case _ => schema
  }

  private def resolveSchemas(x: ClassSchema): Seq[(ClassSchema, Breadcrumb)] = x.properties.flatMap { p =>
    val (itemSchema, _) = cardinalityAndItemSchema(p.schema, p.metadata)
    val resolvedItemSchema: Schema = resolveSchema(itemSchema)
    classSchemasIn(resolvedItemSchema).map(s => (s, Breadcrumb(x, p)))
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

  private val cachedEntities: collection.mutable.Map[Schema, Option[String]] = collection.mutable.Map.empty
  private def getEntity(parentSchema: ClassSchema, schema: ClassSchema, shallowEntities: ClassSchema => Boolean) = if (shallowEntities(parentSchema)) {
    synchronized {
      cachedEntities.getOrElseUpdate(schema, OpiskeluoikeusSchemaFinder(schema, shallowEntities).findOpiskeluoikeusSchema.map(_.simpleName)).getOrElse(schema.simpleName)
    }
  } else {
    schema.simpleName
  }

  case class OpiskeluoikeusSchemaFinder(itemSchema: ClassSchema, shallowEntities: ClassSchema => Boolean) {
    def findOpiskeluoikeusSchema: Option[ClassSchema] =
      opiskeluoikeusSchemas.find(ooSchema => containsItem(nonShallowItemsFrom(ooSchema)))

    private def nonShallowItemsFrom(s: ClassSchema): Seq[ClassSchema] = resolveSchemas(s).map(_._1)
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

  private lazy val opiskeluoikeusSchemas = resolveSchemas(mainSchema).map(_._1).filter(isOpiskeluoikeusSchema)
  private def isOpiskeluoikeusSchema(s: ClassSchema) =
    classOf[Opiskeluoikeus].isAssignableFrom(Class.forName(s.fullClassName))
}
