package fi.oph.tor.util

import java.lang.reflect.{InvocationHandler, Method}
import fi.oph.tor.log.Loggable
import scala.reflect.ClassTag

case class Invocation(val method: Method, val args: List[AnyRef], val target: AnyRef) {
  def invoke: AnyRef = method.invoke(target, args:_*)
  override lazy val toString: String = method.getName + "(" + args.map(Loggable.describe).mkString(", ") +")"
  override lazy val hashCode = toString.hashCode
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
        val found = handlers.contains(interface)
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