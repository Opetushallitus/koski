package fi.oph.tor.util

import java.lang.reflect.{InvocationHandler, Method}
import fi.oph.tor.json.Json
import scala.reflect.ClassTag

case class Invocation(val method: Method, val args: List[AnyRef], val target: AnyRef) {
  def invoke: AnyRef = method.invoke(target, args:_*)
  override def toString: String = method.getName + "(" + args.map(obj => try {Json.write(obj)} catch { case e => obj.toString }).mkString(", ") +")"
}

object Proxy {
  type InvocationHandler = (Invocation => AnyRef)
  type ProxyHandler = (Invocation, InvocationHandler) => AnyRef

  def createProxy[T <: AnyRef](target: T, handler: ProxyHandler)(implicit tag: ClassTag[T]) = {
    java.lang.reflect.Proxy.newProxyInstance(
      target.getClass.getClassLoader,
      Array(tag.runtimeClass.asInstanceOf[Class[T]]),
      Proxy(target, handler)).asInstanceOf[T]
  }

  private def createComponent[T](intf: Class[T], proxy: Proxy): T =
    java.lang.reflect.Proxy.newProxyInstance(
      proxy.target.getClass.getClassLoader,
      Array(intf),
      proxy).asInstanceOf[T]
}

case class Proxy(target: AnyRef, handler: Proxy.ProxyHandler) extends InvocationHandler {
  def invoke(proxy: AnyRef, m: Method, args: Array[AnyRef]): AnyRef = {
    val argList: List[AnyRef] = if (args == null) { Nil } else { args.toList }
    def defaultHandler(invocation: Invocation) = invocation.invoke
    handler(Invocation(m, argList, target), defaultHandler)
  }
}