package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._
import fi.oph.koski.util.Files
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._

import scala.collection.mutable.MutableList
import scala.xml.Elem

object KoskiSchemaDocumentHtml {
  def mainSchema = KoskiSchema.schema
  def html = {
    <html>
      <head>
        <link type="text/css" rel="stylesheet" href="/koski/css/schema-printable.css"/>
      </head>
      <body>
        <h1>Koski-tietomalli</h1>
        { schemaHtml(mainSchema) }
      </body>
    </html>
  }

  def schemaHtml(schema: ClassSchema) = {
    val backlog = new MutableList[String]
    buildBacklog(mainSchema, backlog)
    backlog.map(name => mainSchema.getSchema(name).get.asInstanceOf[ClassSchema]).map(classHtml)
  }

  private def buildBacklog(x: ClassSchema, backlog: MutableList[String]): Unit = {
    val name = x.fullClassName
    if (!backlog.contains(name)) {
      backlog += name

      val moreSchemas: Seq[ClassSchema] = x.properties.flatMap { p =>
        val (itemSchema, _) = cardinalityAndItemSchema(p.schema, p.metadata)
        val resolvedItemSchema = resolveSchema(itemSchema)
        classSchemasIn(resolvedItemSchema)
      }

      moreSchemas.foreach { s =>
        buildBacklog(s, backlog)
      }
    }
  }

  private def classSchemasIn(schema: Schema): List[ClassSchema] = schema match {
    case s: ClassSchema => List(s)
    case s: AnyOfSchema => s.alternatives.map {
      case s: ClassSchema => s
      case s: ClassRefSchema => resolveSchema(s).asInstanceOf[ClassSchema]
    }
    case _ => Nil
  }


  def classHtml(schema: ClassSchema) = <div>
    <h3 id={schema.simpleName}>{schema.title}</h3>
    <table>
      <thead>
        <tr>
          <th>Nimi</th>
          <th>Lukumäärä</th>
          <th>Tyyppi</th>
          <th>Kuvaus</th>
        </tr>
      </thead>
      <tbody>
        {
          schema.properties.map { p =>
            val (itemSchema, cardinality) = cardinalityAndItemSchema(p.schema, p.metadata)
            val resolvedItemSchema = resolveSchema(itemSchema)
            <tr>
              <td>{p.title}</td>
              <td>{cardinality}</td>
              <td>
                {schemaTypeHtml(resolvedItemSchema)}
                {metadataHtml(p.metadata ++ p.schema.metadata)}
              </td>
              <td>
                {descriptionHtml(p)}
              </td>
            </tr>
          }
        }
      </tbody>
    </table>
  </div>

  def schemaTypeHtml(s: Schema): Elem = s match {
    case s: ClassSchema => <a href={"#" + s.simpleName}>{s.title}</a>
    case s: AnyOfSchema if (s.fullClassName == classOf[LocalizedString].getName) => <span>lokalisoitu teksti</span>
    case s: AnyOfSchema => <span>{s.alternatives.map(a => <div>{schemaTypeHtml(resolveSchema(a))}</div>)}</span>
    case s: StringSchema => <span>merkkijono</span> // TODO: schemarajoitukset annotaatioista jne
    case s: NumberSchema => <span>numero</span>
    case s: BooleanSchema => <span>true/false</span>
    case s: DateSchema => <span>päivämäärä</span>
  }

  private def resolveSchema(schema: Schema): Schema = schema match {
    case s: ClassRefSchema => mainSchema.getSchema(s.fullClassName).get
    case _ => schema
  }

  private def cardinalityAndItemSchema(s: Schema, metadata: List[Metadata]):(ElementSchema, Cardinality) = s match {
    case s@ListSchema(itemSchema) => (itemSchema.asInstanceOf[ElementSchema], Cardinality(minItems(s, metadata), maxItems(s, metadata)))
    case OptionalSchema(i: ListSchema) =>
      val (itemSchema, Cardinality(min, max)) = cardinalityAndItemSchema(i, metadata)
      (itemSchema, Cardinality(0, max))
    case OptionalSchema(itemSchema: ElementSchema) =>
      (itemSchema, Cardinality(0, Some(1)))
    case s: ElementSchema => (s, Cardinality(1, Some(1)))
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
        case ReadOnly(desc) => Some(<div class="readonly">{desc}</div>)
        case k: KoodistoUri =>Some(<div class="koodisto">Koodisto: {k.asLink}</div>)
        case k: KoodistoKoodiarvo =>Some(<div class="koodiarvo">Hyväksytty koodiarvo: {k.arvo}</div>)
        case o: OksaUri => Some(<div class="oksa">Oksa: {o.asLink}</div>)
        case _ => None
      }
    }
  }

  private def descriptionHtml(p: Property) = {
    {
      (p.metadata ++ p.schema.metadata).flatMap {
        case Description(desc) => Some(<span class="description">{desc}</span>)
        case _ => None
      }
    }
  }

  case class Cardinality(min: Int, max: Option[Int]) {
    override def toString: String = (min, max) match {
      case (1, Some(1)) => "1"
      case (min, Some(max)) => s"$min..$max"
      case (min, None) => s"$min..n"
    }
  }
}

object KoskiSchemaDocumentHtmlPrinter extends App {
  Files.writeFile("schema.html", KoskiSchemaDocumentHtml.html.toString)
}