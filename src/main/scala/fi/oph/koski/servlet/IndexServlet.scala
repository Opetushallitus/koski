package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Cryptographic
import fi.oph.koski.util.JsStringInterpolation.{JsStringInterpolation, setWindowVar}
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed



class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with OmaOpintopolkuSupport {
  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToVirkailijaLogin
    }
  }

  get("/") {
    if (!isAuthenticated) {
      setLangCookieFromDomainIfNecessary
      landerHtml
    } else {
      val url = if (koskiSessionOption.exists(_.user.kansalainen)) {
        "/koski/omattiedot"
      } else {
        "/koski/virkailija"
      }
      redirect(url)
    }
  }

  get("/virkailija") {
    if (koskiSessionOption.exists(_.hasKelaAccess)) {
      redirect("/koski/kela")
    } else {
      indexHtml
    }
  }

  get("/validointi") {
    indexHtml
  }

  get("/uusioppija") {
    indexHtml
  }

  get("/oppija/:oid") {
    indexHtml
  }

  get("/tiedonsiirrot*") {
    indexHtml
  }

  get("/raportit*") {
    indexHtml
  }

  get("/kela*") {
    indexHtml
  }

  private def indexHtml =
    htmlIndex("koski-main.js", raamit = virkailijaRaamit)

  private def landerHtml = {
    val nonce = Cryptographic.nonce
    htmlIndex(
      scriptBundleName = "koski-lander.js",
      raamit = oppijaRaamit,
      scripts = <script nonce={nonce} id="auth">
        {setWindowVar("kansalaisenAuthUrl", "/koski/login/oppija")}
      </script>,
      responsive = true,
      nonce = nonce
    )
  }
}
