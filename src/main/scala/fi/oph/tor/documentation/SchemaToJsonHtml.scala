package fi.oph.tor.documentation

import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Description
import fi.oph.tor.json.Json
import fi.oph.tor.schema.{KoodistoUri, OksaUri, ReadOnly}

import scala.xml.Elem

object SchemaToJsonHtml {
  def buildHtml(schema: ClassSchema, exampleData: AnyRef): List[Elem] = {
    SchemaToJsonHtml.buildHtml(Property("", schema, Nil), exampleData, schema, NodeContext("0", None))
  }

  private def buildHtml(property: Property, obj: Any, schema: ClassSchema, context: NodeContext): List[Elem] = (obj, property.schema) match {
    case (o: AnyRef, t:ClassSchema) => buildHtmlForObject(property, o, schema, context)
    case (xs: Iterable[_], t:ListSchema) => buildHtmlForArray(property, xs, schema, context)
    case (x: Number, t:NumberSchema) => buildValueHtml(property, x, context)
    case (x: Boolean, t:BooleanSchema) => buildValueHtmlString(property, x.toString, context)
    case (x: AnyRef, t:DateSchema) => buildValueHtml(property, x, context)
    case (x: String, t:StringSchema) => buildValueHtml(property, x, context)
    case (x: Option[_], t:OptionalSchema) => buildHtml(property.copy(schema = t.itemSchema), x.get, schema, context)
    case (x: AnyRef, t:AnyOfSchema) => buildHtml(property.copy(schema = findOneOfSchema(t, x)), x, schema, context)
    case (x: AnyRef, t:ClassRefSchema) => buildHtml(property.copy(schema = schema.getSchema(t.fullClassName).get), x , schema, context)
    case _ =>
      throw new RuntimeException("Unexpected input: " + obj + ", " + property.schema)
  }

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }


  private def keyHtml(key: String) = key match {
    case ""          => ""
    case _           => <span class="key">{key}: </span>
  }

  private def buildHtmlForObject(property: Property, obj: AnyRef, schema: ClassSchema, context: NodeContext): List[Elem] = {
    val propertyElems: List[Elem] = {
      val classSchema: ClassSchema = property.schema.asInstanceOf[ClassSchema]
      val propertiesWithValue: List[(Int, Property, AnyRef)] = classSchema.properties.zipWithIndex.flatMap { case (property: Property, index: Int) =>
        val value = classSchema.getPropertyValue(property, obj)
        value match {
          case None => None
          case _ => Some((index, property, value))
        }
      }

      propertiesWithValue.flatMap { case (i, property, value) =>
        buildHtml(property, value, schema, context.child)
      }

    }
    List(tr(<span><span class="collapsible"></span>{keyHtml(property.key)}{{</span>, property.metadata ++ property.schema.metadata, context)) ++ propertyElems ++ List(tr(<span>{"}"}</span>, Nil, context))
  }

  private def buildHtmlForArray(property: Property, xs: Iterable[_], schema: ClassSchema, context: NodeContext): List[Elem] = {
    val propertyElems: List[Elem] = xs.flatMap { item =>
        buildHtml(Property("", property.schema.asInstanceOf[ListSchema].itemSchema, Nil), item, schema, context.child)
      }.toList

    List(tr(<span><span class="collapsible"></span>{keyHtml(property.key)}[</span>, property.metadata, context)) ++ propertyElems ++ List(tr(<span>{"]"}</span>, Nil, context))
  }



  private def metadataHtml(metadatas: List[Metadata]) = {
    {
      metadatas.flatMap {
        case Description(desc) => Some(<span class="description">{desc}</span>)
        case ReadOnly(desc) => Some(<span class="readonly">{desc}</span>)
        case k: KoodistoUri =>Some(<span class="koodisto">Koodisto: {k.asLink}</span>)
        case o: OksaUri => Some(<span class="oksa">Oksa: {o.asLink}</span>)
        case _ => None
      }
    }
  }

  private def buildValueHtml(property: Property, value: AnyRef, context: NodeContext) = buildValueHtmlString(property, Json.write(value), context)

  private def buildValueHtmlString(property: Property, value: String, context: NodeContext) = {
    List(tr(<span>{keyHtml(property.key)} <span class="value">{value}</span></span>, property.metadata, context))
  }

  private def tr(content:Elem, metadata:List[Metadata], context: NodeContext) = {
    <tr class={"json-row " + context.path}><td>{0.to(context.depth).map(i => <span class="indent"></span>)}{content}</td><td class="metadata">{metadataHtml(metadata)}</td></tr>
  }
}

case class NodeContext(id: String, parent: Option[NodeContext]) {
  var idCounter = 0

  def depth: Int = parent match {
      case Some(p) => p.depth + 1
      case None => 0
  }

  def child = {
    NodeContext(id + "-" + generateId(), Some(this))
  }

  def path:String = parent match {
    case Some(p) => p.path + " node-" + id
    case None => "node-" + id
  }

  private def generateId() = {
    idCounter = idCounter + 1
    idCounter
  }
}

