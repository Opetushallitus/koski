package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic._
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}

import scala.xml.Elem

object SchemaToJsonHtml {


  def buildHtml(property: Property, obj: Any, schema: ScalaJsonSchema, context: NodeContext): List[Elem] = (obj, property.tyep) match {
    case (o: AnyRef, t:ClassType) => buildHtmlForObject(property, o, schema, context)
    case (xs: Iterable[_], t:ListType) => buildHtmlForArray(property, xs, schema, context)
    case (x: Number, t:NumberType) => buildValueHtml(property, x, context)
    case (x: Boolean, t:BooleanType) => buildValueHtmlString(property, x.toString, context)
    case (x: AnyRef, t:DateType) => buildValueHtml(property, x, context)
    case (x: String, t:StringType) => buildValueHtml(property, x, context)
    case (x: Option[_], t:OptionalType) => buildHtml(property.copy(tyep = t.itemType), x.get, schema, context)
    case (x: AnyRef, t:OneOf) => buildHtml(property.copy(tyep = t.matchType(x)), x, schema, context)
    case (x: AnyRef, t:ClassTypeRef) => buildHtml(property.copy(tyep = schema.createSchema(t.fullClassName)), x , schema, context)
    case _ => throw new RuntimeException
  }

  private def keyHtml(key: String) = key match {
    case ""          => ""
    case key         => <span class="key">{key}: </span>
  }

  private def buildHtmlForObject(property: Property, obj: AnyRef, schema: ScalaJsonSchema, context: NodeContext): List[Elem] = {
    val propertyElems: List[Elem] = {
      val classType: ClassType = property.tyep.asInstanceOf[ClassType]
      val propertiesWithValue: List[(Int, Property, AnyRef)] = classType.properties.zipWithIndex.flatMap { case (property: Property, index: Int) =>
        val value = classType.getPropertyValue(property, obj)
        value match {
          case None => None
          case x => Some((index, property, x))
        }
      }

      propertiesWithValue.flatMap { case (i, property, value) =>
        buildHtml(property, value, schema, context.child)
      }

    }
    List(tr(<span><span class="collapsible"></span>{keyHtml(property.key)}{{</span>, property.metadata ++ property.tyep.metadata, context)) ++ propertyElems ++ List(tr(<span>{"}"}</span>, Nil, context))
  }

  private def buildHtmlForArray(property: Property, xs: Iterable[_], schema: ScalaJsonSchema, context: NodeContext): List[Elem] = {
    val propertyElems: List[Elem] = xs.flatMap { item =>
        buildHtml(Property("", property.tyep.asInstanceOf[ListType].itemType, Nil), item, schema, context.child)
      }.toList

    List(tr(<span><span class="collapsible"></span>{keyHtml(property.key)}[</span>, property.metadata, context)) ++ propertyElems ++ List(tr(<span>{"]"}</span>, Nil, context))
  }



  private def metadataHtml(metadatas: List[Metadata]) = {
    {
      metadatas.flatMap {
        case Description(desc) => Some(<span class="description">{desc}</span>)
        case ReadOnly(desc) => Some(<span class="readonly">{desc}</span>)
        case KoodistoUri(koodistoNimi) =>Some(<span class="koodisto">Koodisto: <a href={"https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/"+koodistoNimi+"/1"}>{koodistoNimi}</a></span>)
        case (o @ OksaUri(tunnus, käsite)) => Some(<span class="oksa">Oksa: {o.asLink}</span>)
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

