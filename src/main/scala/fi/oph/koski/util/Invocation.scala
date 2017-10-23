package fi.oph.koski.util

import java.lang.reflect.Method

import fi.oph.koski.log.Loggable

case class Invocation(val f: NamedFunction, val args: List[AnyRef]) {
  def invoke: AnyRef = f(args)
  override lazy val toString: String = f.name + "(" + args.map(Loggable.describe).mkString(", ") + ")"
  override lazy val hashCode = toString.hashCode
}

object Invocation {
  def apply(method: Method, args: List[AnyRef], target: AnyRef): Invocation = {
    Invocation(ObjectMethod(method, target), args)
  }

  def apply[A <: AnyRef, B <: AnyRef](f: A => B, input: A): Invocation = {
    Invocation(AnonymousFunction(f), List(input))
  }
}

trait NamedFunction {
  def apply(args: List[AnyRef]): AnyRef
  def name: String
}

case class ObjectMethod(method: Method, target: AnyRef) extends NamedFunction {
  def apply(args: List[AnyRef]) = method.invoke(target, args:_*)
  def name = method.getName
}

case class AnonymousFunction[A <: AnyRef, B <: AnyRef](f: A => B) extends NamedFunction {
  def name = "anonymous function"
  def apply(args: List[AnyRef]) = {
    f.apply(args(0).asInstanceOf[A])
  }
}