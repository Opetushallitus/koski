package fi.oph.koski.html

import fi.oph.koski.util.JsStringInterpolation.JsStringInterpolation
import fi.oph.koski.util.RawJsString
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.Node

trait PiwikNodes {
  def piwikSiteId: String

  def piwikTrackingScriptLoader(piwikHttpStatusCode: Option[Int] = None): Seq[Node] =
    <script type="text/javascript">
      {CommentedPCData(js"""
      window._paq = window._paq || []
      _paq.push(['trackPageView', ${mkPageTitleForJsEval(piwikHttpStatusCode)}])
      ;(function() { $mkPiwikInit })()
      """)}
    </script>

  def piwikTrackErrorObject: Seq[Node] =
    <script type="text/javascript">
      {CommentedPCData(
      """
      window.koskiError && window._paq && _paq.push(['trackEvent', 'LoadError', JSON.stringify({
        location: '' + document.location,
        httpStatus: koskiError.httpStatus,
        text: koskiError.text
      })])
      """)}
    </script>

  private def mkPageTitleForJsEval(httpStatusCode: Option[Int]): RawJsString = {
    val prefix = RawJsString("document.location.pathname")
    httpStatusCode.map(code => jsPart"$prefix + ${s" ($code)"}").getOrElse(prefix)
  }

  private def mkPiwikInit: RawJsString =
    if (piwikSiteId.isEmpty) {
      // allow access to `window._paq` for tests, delete it after timeout to conserve memory
      // if this value is too short, piwikTrackingSpec will fail occasionally (especially when run on slow server)
      jsPart"""
      setTimeout(function removePiwik() { delete window._paq }, 20000)
      """
    } else {
      // piwikSiteId is overriden with one of opintopolku-sides siteIds if koski runs in one of the following domains
      // opintopolku.fi, studieinfo.fi, studyinfo.fi, virkailija.opintopolku.fi, virkailija.testiopintopolku.fi, or domain starting with 'testi'
      jsPart"""
      var siteIds = {'opintopolku.fi': '4', 'studieinfo.fi': '13', 'studyinfo.fi': '14', 'virkailija.opintopolku.fi': '3', 'virkailija.testiopintopolku.fi': '5'}
      var siteId = siteIds[document.domain] || (document.domain.indexOf('testi') === 0 ? '1' : $piwikSiteId)
      var u = 'https://analytiikka.opintopolku.fi/piwik/'
      _paq.push(['setTrackerUrl', u+'piwik.php'])
      _paq.push(['setSiteId', siteId])
      var d = document, g = d.createElement('script'), s = d.getElementsByTagName('script')[0]
      g.type = 'text/javascript'; g.async=true; g.defer=true; g.src = u+'piwik.js'; s.parentNode.insertBefore(g, s)
      """
    }
}
