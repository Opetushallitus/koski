package fi.oph.koski.tiedonsiirto

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.email.{Email, EmailContent, EmailRecipient, MockEmailSender}
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.schema.OidOrganisaatio
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class TiedonsiirtoFailureMailerSpec extends FreeSpec with Matchers with BeforeAndAfterEach {
  private val mailer = new TiedonsiirtoFailureMailer(KoskiApplicationForTests.config, KoskiApplicationForTests.opintopolkuHenkilöFacade)
  "Lähettää sähköpostia" - {
    "oppilaitoksen KOSKI-pääkäyttäjä:lle jos mahdollista" in {
      mailer.sendMail(OidOrganisaatio(helsinginKaupunki), Some(OidOrganisaatio(stadinAmmattiopisto)))
      MockEmailSender.checkMail should equal(List(expectedEmail("stadinammattiopisto-admin@example.com")))
    }

    "sitten juuriorganisaation KOSKI-pääkäyttäjä:lle jos mahdollista" in {
      mailer.sendMail(OidOrganisaatio(helsinginKaupunki), Some(OidOrganisaatio(omnia)))
      MockEmailSender.checkMail should equal(List(expectedEmail("stadin-pää@example.com")))
    }

    "viimeiseksi juuriorganisaation Vastuukayttaja:lle" in {
      mailer.sendMail(OidOrganisaatio(jyväskylänYliopisto), Some(OidOrganisaatio(jyväskylänNormaalikoulu)))
      MockEmailSender.checkMail should equal(List(expectedEmail("jyväs-vastuu@example.com")))
    }
  }

  override def beforeEach = MockEmailSender.checkMail

  private def expectedEmail(emailAddress: String) = Email(
    EmailContent(
      "no-reply@opintopolku.fi",
      "Virheellinen Koski-tiedonsiirto",
      "<p>Automaattisessa tiedonsiirrossa tapahtui virhe.</p><p>Käykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa: http://localhost:7021/koski/tiedonsiirrot</p>",
      html = true),
    List(EmailRecipient(emailAddress)))
}
