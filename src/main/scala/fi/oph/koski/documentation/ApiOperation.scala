package fi.oph.koski.documentation

import fi.oph.koski.http.ErrorCategory
import fi.oph.scalaschema.annotation.SyntheticProperty

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

object ApiOperation {
  def apply(method: String, path: String, summary: String, doc: Elem, examples: List[ExampleBase], parameters: List[ApiOperationParameter], statusCodes: List[ErrorCategory]): ApiOperation =
      ApiOperation(method, path, summary, doc.toString, examples, parameters, statusCodes)
}
case class ApiOperation(method: String, path: String, summary: String, doc: String, examples: List[ExampleBase], parameters: List[ApiOperationParameter], statusCodes: List[ErrorCategory]) extends ApiDefinition {
  def operations = List(this)
  @SyntheticProperty
  def errorCategories: List[ErrorCategory] = statusCodes.flatMap(_.flatten)
  @SyntheticProperty
  def operation: String = path
}

sealed trait ApiOperationParameter {
  def name: String
  def description: String
  def examples: List[String]
  def example = examples.head
  def hasMultipleExamples: Boolean = examples.size > 1
  def `type`: String
}

case class PathParameter(name: String, description: String, examples: List[String]) extends ApiOperationParameter {
  @SyntheticProperty
  def `type` = "path"
}
case class QueryParameter(name: String, description: String, examples: List[String]) extends ApiOperationParameter {
  @SyntheticProperty
  def `type` = "query"
}

