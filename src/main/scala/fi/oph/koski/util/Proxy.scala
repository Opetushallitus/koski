package fi.oph.koski.util

import java.lang.reflect.{InvocationHandler, Method}
import fi.oph.koski.log.Loggable
import scala.reflect.ClassTag

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

object Proxy {
  type ProxyHandler = Invocation => AnyRef

  def createProxy[T <: AnyRef](target: T, handler: ProxyHandler)(implicit tag: ClassTag[T]) = {
    createMultiProxy(Map(tag.runtimeClass.asInstanceOf[Class[T]] -> (target, handler))).asInstanceOf[T]
  }

  def createMultiProxy(handlers: Map[Class[_], (AnyRef, ProxyHandler)]) = {
    val handler = new InvocationHandler {
      override def invoke(proxy: AnyRef, method: Method, args: Array[AnyRef]) = {
        val interface: Class[_] = method.getDeclaringClass
        val (target, handler) = handlers(interface)
        new Proxy(target, handler).invoke(proxy, method, args)
      }
    }
    java.lang.reflect.Proxy.newProxyInstance(
      getClass.getClassLoader,
      handlers.keys.toArray,
      handler)
  }
}

class Proxy(target: AnyRef, handler: Proxy.ProxyHandler) extends InvocationHandler {
  def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = {
    val argList: List[AnyRef] = if (args == null) { Nil } else { args.toList }
    handler(Invocation(m, argList, target))
  }
}