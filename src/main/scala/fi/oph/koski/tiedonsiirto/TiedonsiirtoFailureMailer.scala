package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import com.typesafe.config.Config
import fi.oph.koski.email._
import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.OrganisaatioWithOid

class TiedonsiirtoFailureMailer(config: Config, authenticationServiceClient: OpintopolkuHenkilöFacade) extends Logging {
  private val sendTimes = scala.collection.mutable.Map[String, LocalDateTime]()
  private val sender = EmailSender(config)
  private val koskiPääkäyttäjät = "KOSKI-pääkäyttäjä"
  private val vastuukayttajat = "Vastuukayttajat"

  def sendMail(rootOrg: OrganisaatioWithOid, oppilaitos: Option[OrganisaatioWithOid]): Unit = {
    if (!shouldSendMail(rootOrg, oppilaitos)) {
      return
    }
    val emails = oppilaitosSähköpostit(oppilaitos) match {
      case Nil => rootOrgSähköpostit(rootOrg)
      case emailAddresses => emailAddresses
    }

    emails.distinct match {
      case Nil => logger.info(s"Organisaatioille ${(rootOrg.oid +: oppilaitos.toList.map(_.oid)).mkString(" ja ")} ei löydy sähköpostiosoitetta")
      case emailAddresses =>
        val mail: Email = Email(EmailContent(
          "no-reply@opintopolku.fi",
          "Virheellinen Koski-tiedonsiirto",
          "<p>Automaattisessa tiedonsiirrossa tapahtui virhe.</p><p>Käykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa: " + config.getString("koski.root.url") + "/tiedonsiirrot</p>",
          html = true
        ), emailAddresses.map(EmailRecipient))
        logger.debug(s"Sending failure mail to ${emailAddresses.mkString(",")}")
        sender.sendEmail(mail)
    }
  }

  private def shouldSendMail(rootOrg: OrganisaatioWithOid, organisaatio: Option[OrganisaatioWithOid]) = sendTimes.synchronized {
    val limit = now().minusHours(24)
    val key = rootOrg.oid + organisaatio.map(_.oid).getOrElse("")
    if (!sendTimes.getOrElse(key, limit).isAfter(limit)) {
      sendTimes.put(key, now())
      true
    } else {
      false
    }
  }

  private def rootOrgSähköpostit(k: OrganisaatioWithOid) = organisaatioSähköpostit(k.oid, koskiPääkäyttäjät) match {
    case Nil => organisaatioSähköpostit(k.oid, vastuukayttajat)
    case emails => emails
  }

  private def oppilaitosSähköpostit(org: Option[OrganisaatioWithOid]) =
    org.toList.flatMap(o => organisaatioSähköpostit(o.oid, koskiPääkäyttäjät))

  private def organisaatioSähköpostit(oid: String, ryhmä: String): List[String] = {
    authenticationServiceClient.organisaationSähköpostit(oid, ryhmä)
  }
}
