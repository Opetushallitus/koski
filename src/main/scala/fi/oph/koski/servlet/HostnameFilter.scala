package fi.oph.koski.servlet

import java.net.InetAddress
import javax.servlet._
import javax.servlet.http.HttpServletResponse

class HostnameFilter extends Filter {
  val hostname = InetAddress.getLocalHost.getHostName

  override def init(filterConfig: FilterConfig) {}

  override def destroy {}

  override def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) = {
    servletResponse.asInstanceOf[HttpServletResponse].setHeader("X-Backend-Server", hostname)
    filterChain.doFilter(servletRequest, servletResponse)
  }
}
