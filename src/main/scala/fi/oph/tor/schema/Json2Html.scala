package fi.oph.tor.schema

import fi.oph.tor.json.Json._
import org.json4s.JValue
import org.json4s.JsonAST._

import scala.xml.Elem

object Json2Html extends App {

  println(
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <style>
          ul {{list-style-type: none; margin: 0; padding-left: 20px}}
          div, li {{display: inline}}
          li.spacer::after {{display: block; content: ''}}
        </style>
      </head>
      <body>
        {buildHtml(toJValue(TorOppijaExamples.full))}
      </body>
    </html>
  )

  def name(f: String) = <span class="name">"{f}"</span>
  def intersperse[T](l: List[T], spacer: T) = l.zipWithIndex.flatMap {
    case (x, index) => if(index == 0) {List(x)} else {List(spacer, x)}
  }

  def buildHtml(json: JValue): Elem = {
    json match {
      case JObject(fields) => {
        <div>
          {" { "}
          <ul>{
            intersperse(fields.map {
              case (f, JObject(s))  => <li>{name(f)} : { buildHtml(JObject(s)) }</li>
              case (f, JString(s))  => <li>{name(f)} : <span class="value">{s}</span></li>
              case (f, JDouble(s))  => <li>{name(f)} : <span class="value">{s}</span></li>
              case (f, JDecimal(s)) => <li>{name(f)} : <span class="value">{s}</span></li>
              case (f, JInt(s))     => <li>{name(f)} : <span class="value">{s}</span></li>
              case (f, JBool(s))    => <li>{name(f)} : <span class="value">{s}</span></li>
              case (f, JArray(a))   => <li>{name(f)} : [{intersperse(a.map(buildHtml), ",")}]</li>
              case _                => <span></span>
            }, <li class="spacer">,</li>)
          }</ul>
          {" } "}
        </div>
      }
    }
  }
}
