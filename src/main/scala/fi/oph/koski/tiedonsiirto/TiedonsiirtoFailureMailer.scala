package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import com.typesafe.config.Config
import fi.oph.koski.email._

class TiedonsiirtoFailureMailer(config: Config) {
  val sendTimes = scala.collection.mutable.Map[String, LocalDateTime]()
  val sender = EmailSender(config)

  def sendMail(organisaatioOid: String): Unit = {
    val emailAddress = "TODO"
    sendTimes.synchronized {
      if (shouldSendMail(emailAddress)) {
        sendTimes.put(emailAddress, now())
        val mail: Email = Email(EmailContent(
          "no-reply@opintopolku.fi",
          "Virheellinen KOSKI tiedonsiirto",
          "Automaattisessa tiedonsiirrossa tapahtui virhe.\nKäykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa TODO.",
          html = false
        ), List(EmailRecipient(emailAddress)))
        sender.sendEmail(mail)
      }
    }
  }

  def shouldSendMail(email: String) =
    sendTimes.get(email) match {
      case Some(time) if time.isBefore(now().minusHours(24)) => true
      case None => true
      case _ => false
    }
}