package fi.oph.koski.util

import java.lang.reflect.{InvocationTargetException, Method, UndeclaredThrowableException}
import java.util.concurrent.ExecutionException

import fi.oph.common.log.Loggable

import scala.util.control.NonFatal

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

  def apply[B <: AnyRef](f: () => B): Invocation = {
    Invocation(NoArgsFunction(f), List())
  }
}

trait NamedFunction {
  def apply(args: List[AnyRef]): AnyRef
  def name: String
}

case class ObjectMethod(method: Method, target: AnyRef) extends NamedFunction {
  def apply(args: List[AnyRef]) = {
    try {
      method.invoke(target, args:_*)
    } catch {
      case NonFatal(e) => throw skipReflectionExceptions(e)
    }
  }
  def name = method.getName

  private def skipReflectionExceptions(t: Throwable): Throwable =
    if (isReflectionException(t) && t.getCause != null) skipReflectionExceptions(t.getCause) else t
  private def isReflectionException(t: Throwable): Boolean =
    t.isInstanceOf[InvocationTargetException] || t.isInstanceOf[ExecutionException] || t.isInstanceOf[UndeclaredThrowableException]
}

case class NoArgsFunction[B <: AnyRef](f: () => B) extends NamedFunction {
  override def name: String = "noargsanonymous"
  def apply(args: List[AnyRef]) = {
    f.apply()
  }
}

case class AnonymousFunction[A <: AnyRef, B <: AnyRef](f: A => B) extends NamedFunction {
  def name = "anonymous"
  def apply(args: List[AnyRef]) = {
    f.apply(args(0).asInstanceOf[A])
  }
}
