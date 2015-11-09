package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic._
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}

import scala.xml.Elem

object SchemaToJsonHtml {


  def buildHtml(property: Property, obj: Any, schema: ScalaJsonSchema, indentation: Int): List[Elem] = (obj, property.tyep) match {
    case (o: AnyRef, t:ClassType) => buildHtmlForObject(property, o, schema, indentation)
    case (xs: Iterable[_], t:ListType) => buildHtmlForArray(property, xs, schema, indentation)
    case (x: Number, t:NumberType) => buildValueHtml(property, x, indentation)
    case (x: Boolean, t:BooleanType) => buildValueHtmlString(property, x.toString, indentation)
    case (x: AnyRef, t:DateType) => buildValueHtml(property, x, indentation)
    case (x: String, t:StringType) => buildValueHtml(property, x, indentation)
    case (x: Option[_], t:OptionalType) => buildHtml(property.copy(tyep = t.itemType), x.get, schema, indentation)
    case (x: AnyRef, t:OneOf) => buildHtml(property.copy(tyep = t.matchType(x)), x, schema, indentation)
    case (x: AnyRef, t:ClassTypeRef) => buildHtml(property.copy(tyep = schema.createSchema(t.fullClassName)), x , schema, indentation)
    case _ => throw new RuntimeException
  }

  private def intersperse[T](l: List[T], spacer: T) = l.zipWithIndex.flatMap {
    case (x, index) => if(index == 0) {List(x)} else {List(spacer, x)}
  }

  private def keyHtml(key: String) = key match {
    case ""          => ""
    case key         => <span class="key">{key}: </span>
  }

  private def buildHtmlForObject(property: Property, obj: AnyRef, schema: ScalaJsonSchema, indentation: Int): List[Elem] = {
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
        buildHtml(property, value, schema, indentation + 1)
      }

    }
    List(tr(<span>{keyHtml(property.key)}{{</span>, property.metadata ++ property.tyep.metadata, indentation)) ++ propertyElems ++ List(tr(<span>{"}"}</span>, Nil, indentation))
  }

  private def buildHtmlForArray(property: Property, xs: Iterable[_], schema: ScalaJsonSchema, indentation: Int): List[Elem] = {
    val propertyElems: List[Elem] = xs.flatMap { item =>
        buildHtml(Property("", property.tyep.asInstanceOf[ListType].itemType, Nil), item, schema, indentation + 1)
      }.toList

    List(tr(<span>{keyHtml(property.key)}[</span>, property.metadata, indentation)) ++ propertyElems ++ List(tr(<span>{"]"}</span>, Nil, indentation))
  }



  private def metadataHtml(metadatas: List[Metadata]) = {
    <span class="metadata">
      {
      metadatas.flatMap {
        case Description(desc) => Some(<span class="description">{desc}</span>)
        case ReadOnly(desc) => Some(<span class="readonly">{desc}</span>)
        case KoodistoUri(koodistoNimi) =>Some(<a href={"https://testi.virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/"+koodistoNimi+"/1"} class="koodisto">Koodisto: {koodistoNimi}</a>)
        case _ => None
      }
      }
    </span>
  }

  private def buildValueHtml(property: Property, value: AnyRef, indentation: Int) = buildValueHtmlString(property, Json.write(value), indentation)

  private def buildValueHtmlString(property: Property, value: String, indentation: Int) = {
    List(tr(<span>{keyHtml(property.key)} <span class="value">{value}</span></span>, property.metadata, indentation))
  }

  private def tr(content:Elem, metadata:List[Metadata], indentation: Int) = {
    <tr><td>{0.to(indentation).map(i => <span class="indent">&nbsp;&nbsp;&nbsp;</span>)}{content}</td><td class="metadata">{metadataHtml(metadata)}</td></tr>
  }

}
