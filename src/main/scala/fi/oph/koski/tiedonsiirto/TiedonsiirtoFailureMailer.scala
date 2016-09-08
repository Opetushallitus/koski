package fi.oph.koski.tiedonsiirto

import com.typesafe.config.Config
import fi.oph.koski.email._

class TiedonsiirtoFailureMailer(config: Config) {
  val sender = EmailSender(config)
  def sendMail(organisaatioOid: String): Unit = {
    val emailAddress = "TODO"
    sender.sendEmail(Email(EmailContent(
      "no-reply@opintopolku.fi",
      "Virheellinen KOSKI tiedonsiirto",
      "Automaattisessa tiedonsiirrossa tapahtui virhe.\\nKäykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa TODO.",
      html = false
    ), List(EmailRecipient(emailAddress))))
  }
}