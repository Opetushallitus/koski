package fi.oph.tor.util

import java.lang.reflect.{InvocationHandler, Method}
import fi.oph.tor.log.Loggable
import scala.reflect.ClassTag

case class Invocation(val method: Method, val args: List[AnyRef], val target: AnyRef) {
  def invoke: AnyRef = method.invoke(target, args:_*)
  override def toString: String = method.getName + "(" + args.map(Loggable.describe).mkString(", ") +")"
}

object Proxy {
  type ProxyHandler = Invocation => AnyRef

  def createProxy[T <: AnyRef](target: T, handler: ProxyHandler)(implicit tag: ClassTag[T]) = {
    java.lang.reflect.Proxy.newProxyInstance(
      target.getClass.getClassLoader,
      Array(tag.runtimeClass.asInstanceOf[Class[T]]),
      Proxy(target, handler)).asInstanceOf[T]
  }
}

case class Proxy(target: AnyRef, handler: Proxy.ProxyHandler) extends InvocationHandler {
  def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = {
    val argList: List[AnyRef] = if (args == null) { Nil } else { args.toList }
    handler(Invocation(m, argList, target))
  }
}