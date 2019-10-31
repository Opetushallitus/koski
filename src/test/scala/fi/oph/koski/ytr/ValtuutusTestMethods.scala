package fi.oph.koski.ytr

import org.scalatra.test.HttpComponentsClient

trait ValtuutusTestMethods extends HttpComponentsClient {
  def valtuutusCode: String =
    response.headers("Location").head.split("=").last
}
