package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import com.typesafe.config.Config
import fi.oph.koski.email._
import fi.oph.koski.henkilo.OpintopolkuHenkilöFacade
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koulutustoimija, Oppilaitos, OrganisaatioWithOid}

class TiedonsiirtoFailureMailer(config: Config, authenticationServiceClient: OpintopolkuHenkilöFacade) extends Logging {
  private val sendTimes = scala.collection.mutable.Map[String, LocalDateTime]()
  private val sender = EmailSender(config)
  private val koskiPääkäyttäjät = "KOSKI-pääkäyttäjä"
  private val vastuukayttajat = "Vastuukayttajat"

  def sendMail(rootOrg: OrganisaatioWithOid, oppilaitokset: List[OrganisaatioWithOid]): Unit = {
    val emails = oppilaitokset.flatMap(oppilaitosSähköpostit) match {
      case Nil => rootOrgSähköpostit(rootOrg)
      case emailAddresses => emailAddresses
    }

    emails.distinct match {
      case Nil => logger.info(s"Organisaatioille ${(rootOrg :: oppilaitokset).map(_.oid).mkString(", ")} ei löydy sähköpostiosoitetta")
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

  private def shouldSendMail(organisaatio: String) = sendTimes.synchronized {
    val limit = now().minusHours(24)
    if (!sendTimes.getOrElse(organisaatio, limit).isAfter(limit)) {
      sendTimes.put(organisaatio, now())
      true
    } else {
      false
    }
  }

  private def rootOrgSähköpostit(k: OrganisaatioWithOid): List[String] = {
    if (!shouldSendMail(k.oid)) {
      return Nil
    }
    organisaatioSähköpostit(k.oid, koskiPääkäyttäjät) match {
      case Nil => organisaatioSähköpostit(k.oid, vastuukayttajat)
      case emails => emails
    }
  }

  private def oppilaitosSähköpostit(org: OrganisaatioWithOid): List[String] = {
    if (!shouldSendMail(org.oid)) {
      return Nil
    }
    organisaatioSähköpostit(org.oid, koskiPääkäyttäjät)
  }

  private def organisaatioSähköpostit(oid: String, ryhmä: String): List[String] = {
    authenticationServiceClient.organisaationSähköpostit(oid, ryhmä)
  }
}
