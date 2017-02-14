package fi.oph.koski.documentation

import fi.oph.koski.http.ErrorCategory

import scala.xml.Elem

sealed trait ApiDefinition {
  def operations: List[ApiOperation]
}

class ApiGroup extends ApiDefinition {
  private var ops: List[ApiDefinition] = Nil

  def operations: List[ApiOperation] = ops.flatMap(_.operations)

  protected def add[T <: ApiDefinition](op: T) = {
    ops = ops ++ List(op)
    op
  }
}

case class ApiOperation(method: String, path: String, summary: String, doc: Elem, examples: List[Example], parameters: List[ApiOperationParameter], statusCodes: List[ErrorCategory]) extends ApiDefinition {
  def operations = List(this)
}

sealed trait ApiOperationParameter {
  def name: String
  def description: String
  def examples: List[String]
  def example = examples.head
  def hasMultipleExamples: Boolean = examples.size > 1
}

case class PathParameter(name: String, description: String, examples: List[String]) extends ApiOperationParameter
case class QueryParameter(name: String, description: String, examples: List[String]) extends ApiOperationParameter

