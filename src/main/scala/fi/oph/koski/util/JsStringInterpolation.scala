package fi.oph.koski.util

import fi.oph.koski.util.ChainingSyntax._
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.Serialization.write

import scala.xml.{Atom, Unparsed}

object JsStringInterpolation {
  implicit class JsStringInterpolation(val sc: StringContext) extends AnyVal {
    def js(args: Any*): String = {
      val strings = sc.parts.iterator
      val expressions = args
        .map(parseExpression)
        .iterator
      strings
        .interleave(expressions)
        .mkString("")
    }

    def jsAtom(args: Any*): Atom[String] =
      Unparsed(js(args: _*))

    def jsPart(args: Any*): RawJsString =
      RawJsString(js(args: _*))
  }

  def parseExpression(arg: Any): String = {
    arg match {
      case s: RawJsString => s.str
      case o: Object => write(o)
    }
  }

  def setWindowVar(name: String, value: Any) =
    jsAtom"""window[$name] = $value;"""
}

case class RawJsString(str: String)
