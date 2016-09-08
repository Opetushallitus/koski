package fi.oph.koski.tiedonsiirto

import fi.oph.koski.email._

class TiedonsiirtoFailureMailer {
  val sender = MockEmailSender
  def sendMail(organisaatioOid: String): Unit = {
    val emailAddress = "TODO"
    sender.sendEmail(Email(EmailContent(
      "no-reply@opintopolku.fi",
      "Virheellinen KOSKI tiedonsiirto",
      "Automaattisessa tiedonsiirrossa tapahtui virhe.\\nKäykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa https://koskidev.koski.oph.reaktor.fi/koski/tiedonsiirrot.",
      html = false
    ), List(EmailRecipient(emailAddress))))
  }
}