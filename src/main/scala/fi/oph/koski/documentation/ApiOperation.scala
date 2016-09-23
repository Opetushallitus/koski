package fi.oph.koski.documentation

import fi.oph.koski.http.ErrorCategory

import scala.xml.Elem

case class ApiOperation(method: String, path: String, summary: String, doc: Elem, examples: List[Example], parameters: List[ApiOperationParameter], statusCodes: List[ErrorCategory])

sealed trait ApiOperationParameter {
  def name: String
  def description: String
  def example: String
}

case class PathParameter(name: String, description: String, example: String) extends ApiOperationParameter
case class QueryParameter(name: String, description: String, example: String) extends ApiOperationParameter