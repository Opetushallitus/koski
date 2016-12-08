package fi.oph.koski.db

import slick.ast.ScalaBaseType._
import slick.lifted.{Rep, SimpleBinaryOperator}

import scala.language.{higherKinds, implicitConversions}

object ILikeExtension {
  private val opIlike = SimpleBinaryOperator[Boolean]("ILIKE")
  def ilike(left: Rep[String], right: Rep[String]) = opIlike(left, right)
}