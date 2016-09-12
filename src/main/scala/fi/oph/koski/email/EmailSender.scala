package fi.oph.koski.email

import com.typesafe.config.Config
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.json.Json.jsonFormats
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.log.Logging

trait EmailSender {
  def sendEmail(mail: Email)
}

// Huom! Näitä domain-luokkia käytetään tiedon lähettämiseen ryhmäsähköpostipalveluun,
// joten rakenteen muuttaminen rikkoo yhteensopivuuden. Älä siis muuta rakennetta.
case class Email(email: EmailContent, recipient: List[EmailRecipient])
case class EmailContent(from: String, subject: String, body: String, html: Boolean)
case class EmailRecipient(email: String)

object EmailSender {
  def apply(config: Config): EmailSender = if (config.hasPath("ryhmäsähköposti.virkailija.url")) {
    RyhmäsähköpostiSender(config)
  } else {
    MockEmailSender
  }
}

object MockEmailSender extends EmailSender with Logging {
  private var mails: List[Email] = Nil
  override def sendEmail(mail: Email) = this.synchronized {
    logger.info("Sending " + mail)
    mails = mail :: mails
  }
  def checkMail: List[Email] = this.synchronized {
    val newMails = mails
    mails = Nil
    newMails
  }
}

case class RyhmäsähköpostiSender(config: Config) extends EmailSender {
  import Http._

  val http = VirkailijaHttpClient(config.getString("ryhmäsähköposti.virkailija.username"), config.getString("ryhmäsähköposti.virkailija.password"), config.getString("ryhmäsähköposti.virkailija.url"), "/ryhmasahkoposti-service")

  override def sendEmail(envelope: Email): Unit = {
    http.post(uri"/ryhmasahkoposti-service/email", envelope)(json4sEncoderOf[Email], Http.expectSuccess)
  }
}

