package fi.oph.koski.massaluovutus

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MassaluovutusDocumentationSpec extends AnyFreeSpec with Matchers {
  lazy val sections: Map[String, String] = QueryDocumentation.htmlTextSections(KoskiApplicationForTests)

  val virheet = List("Esimerkkiä ei löydy", "!!! NOT FOUND !!!", "Virhe:", ">todo<", "Raporttityyppiä ei löydy")

  "Massaluovutusdokumentaatio" - {
    "kaikki sivut renderöityvät ilman puuttuvia direktiivejä" in {
      sections.keys should contain allOf("massaluovutus_yleista", "massaluovutus_koulutuksenjarjestajat", "massaluovutus_raportit", "massaluovutus_valpas", "massaluovutus_oph")
      sections.foreach { case (name, html) =>
        withClue(name) {
          html should not include "{{"
          virheet.foreach(v => html should not include v)
        }
      }
    }

    "yleiset ohjeet kuvaavat kyselyn elinkaaren ja vastausten kentät" in {
      val html = sections("massaluovutus_yleista")
      html should include("""<h2 id="uuden-kyselyn-luonti">""")
      html should include("""<h2 id="kyselyn-valmistuminen">""")
      html should include("<summary>Vastauksen kentät</summary>")
      html should include(""""status" : "complete"""")
    }

    "koulutuksenjärjestäjien sivu yhdistää saman kyselyn formaatit välilehdiksi" in {
      val html = sections("massaluovutus_koulutuksenjarjestajat")
      html should include("""<h2 id="organisaationOpiskeluoikeudet">Organisaation opiskeluoikeudet</h2>""")
      html should include("""<label for="format-organisaationOpiskeluoikeudet-0">CSV</label>""")
      html should include("""<label for="format-organisaationOpiskeluoikeudet-1">JSON</label>""")
      html should include("""<h2 id="luokallejaaneet">""")
      html should not include "Päällekkäiset opiskeluoikeudet"
      html should include("""POST /koski/api/massaluovutus HTTP/1.1""")
    }

    "raporttisivulla yhteiset parametrit kuvataan kerran ja raportit ryhmitellään koulutusmuodoittain" in {
      val html = sections("massaluovutus_raportit")
      html should include("<summary>Yhteiset parametrit</summary>")
      html should include("""<nav class="toc">""")
      html should include("""<li><a href="#lukionSuoritustiedot">Lukion suoritustietojen tarkistus</a></li>""")
      html should include("""<h2 id="paallekkaisetOpiskeluoikeudet">Päällekkäiset opiskeluoikeudet</h2>""")
      html should include("""<h2 id="lukiokoulutus">Lukiokoulutus</h2>""")
      html should include("""<h3 id="lukionSuoritustiedot">Lukion suoritustietojen tarkistus</h3>""")
      // raporttikohtaisissa taulukoissa ei toisteta yhteisiä kenttiä
      val lukio = html.substring(html.indexOf("""<h3 id="lukionSuoritustiedot">"""), html.indexOf("""<h3 id="lukio2019Suoritustiedot">"""))
      lukio should include("<td><b>alku*</b></td>")
      lukio should not include "<td>password</td>"
      lukio should not include "<td>organisaatioOid</td>"
      lukio should include("<td><b>type*</b></td>")
    }

    "oppivelvollisuuden valvonnan sivu käyttää Valppaan osoitetta" in {
      val html = sections("massaluovutus_valpas")
      html should include("""<h2 id="eiSuoritaOppivelvollisuutta">""")
      html should include("""<h2 id="oppivelvolliset">""")
      html should include("POST /koski/valpas/api/massaluovutus HTTP/1.1")
      html should not include "POST /koski/api/massaluovutus HTTP/1.1"
    }
  }
}
