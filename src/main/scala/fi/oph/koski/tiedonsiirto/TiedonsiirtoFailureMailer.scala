package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import com.typesafe.config.Config
import fi.oph.koski.email._
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.log.Logging

class TiedonsiirtoFailureMailer(config: Config, authenticationServiceClient: AuthenticationServiceClient) extends Logging {
  val sendTimes = scala.collection.mutable.Map[String, LocalDateTime]()
  val sender = EmailSender(config)
  val ryhmä = "Vastuukayttajat"

  def sendMail(organisaatioOid: String): Unit = {

    val emailAddresses: List[String] = authenticationServiceClient.organisaationHenkilötRyhmässä(ryhmä, organisaatioOid).flatMap(_.workEmails)

    emailAddresses match {
      case Nil => logger.info("Organisaatiolle " + organisaatioOid + " ei löydy sähköpostiosoitetta henkilöille jotka kuuluvat ryhmään " + ryhmä)
      case _ =>
        if (shouldSendMail(organisaatioOid)) {
          val mail: Email = Email(EmailContent(
            "no-reply@opintopolku.fi",
            "Virheellinen Koski-tiedonsiirto",
            "<p>Automaattisessa tiedonsiirrossa tapahtui virhe.</p><p>Käykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa: " + config.getString("koski.root.url") + "/tiedonsiirrot</p>",
            html = true
          ), emailAddresses.map(EmailRecipient))
          sender.sendEmail(mail)
        }
    }
  }

  def shouldSendMail(organisaatio: String) = sendTimes.synchronized {
    val limit = now().minusHours(24)
    if (!sendTimes.getOrElse(organisaatio, limit).isAfter(limit)) {
      sendTimes.put(organisaatio, now())
      true
    } else {
      false
    }
  }
}