package fi.oph.koski.tiedonsiirto

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.email._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.OrganisaatioWithOid

class TiedonsiirtoFailureMailer(application: KoskiApplication) extends Logging {
  private val sendTimes = scala.collection.mutable.Map[String, LocalDateTime]()
  private val sender = EmailSender(application.config)
  private val koskiPääkäyttäjät = "koski-oppilaitos-pääkäyttäjä_1494486198456"
  private val vastuukayttajat = "Vastuukayttajat"

  def sendMail(rootOrg: OrganisaatioWithOid, oppilaitos: Option[OrganisaatioWithOid]): Unit = try {
    if (mailEnabled(rootOrg, oppilaitos) && !alreadySent(rootOrg, oppilaitos)) {
      send(rootOrg, oppilaitos)
    }
  } catch {
    case e: Exception => logger.error(e)(s"Problem sending mail to organizations ${orgsToString(rootOrg, oppilaitos)}")
  }

  private def send(rootOrg: OrganisaatioWithOid, oppilaitos: Option[OrganisaatioWithOid]): Unit = {
    val emails = oppilaitosSähköpostit(oppilaitos) match {
      case Nil => rootOrgSähköpostit(rootOrg)
      case emailAddresses => emailAddresses
    }

    emails.distinct match {
      case Nil => logger.info(s"No email address found for organizations ${orgsToString(rootOrg, oppilaitos)}")
      case emailAddresses =>
        val mail: Email = Email(EmailContent(
          "no-reply@opintopolku.fi",
          "Virheellinen Koski-tiedonsiirto",
          "<p>Automaattisessa tiedonsiirrossa tapahtui virhe.</p><p>Käykää ystävällisesti tarkistamassa tapahtuneet tiedonsiirrot osoitteessa: " + application.config.getString("koski.root.url") + "/tiedonsiirrot</p>",
          html = true
        ), emailAddresses.map(EmailRecipient))
        logger.debug(s"Sending failure mail to ${emailAddresses.mkString(",")}")
        sender.sendEmail(mail)
    }
  }

  private def orgsToString(rootOrg: OrganisaatioWithOid, oppilaitos: Option[OrganisaatioWithOid]) =
    (rootOrg +: oppilaitos.toList).map(_.oid).distinct.mkString(" and ")

  private def alreadySent(rootOrg: OrganisaatioWithOid, organisaatio: Option[OrganisaatioWithOid]) = sendTimes.synchronized {
    val limit = now().minusHours(24)
    val key = rootOrg.oid + organisaatio.map(_.oid).getOrElse("")
    if (!sendTimes.getOrElse(key, limit).isAfter(limit)) {
      sendTimes.put(key, now())
      false
    } else {
      true
    }
  }

  private def rootOrgSähköpostit(k: OrganisaatioWithOid) = organisaatioSähköpostit(k.oid, koskiPääkäyttäjät) match {
    case Nil => organisaatioSähköpostit(k.oid, vastuukayttajat)
    case emails => emails
  }

  private def oppilaitosSähköpostit(org: Option[OrganisaatioWithOid]) =
    org.toList.flatMap(o => organisaatioSähköpostit(o.oid, koskiPääkäyttäjät))

  private def organisaatioSähköpostit(oid: String, ryhmä: String): List[String] = {
    application.opintopolkuHenkilöFacade.organisaationSähköpostit(oid, ryhmä)
  }

  private def mailEnabled(rootOrg: OrganisaatioWithOid, oppilaitos: Option[OrganisaatioWithOid]) = {
    val enabledOrgs = application.config.getStringList("tiedonsiirtomail.enabledForOrganizations")
    val orgEnabled = enabledOrgs.contains(rootOrg.oid) || oppilaitos.map(_.oid).exists(enabledOrgs.contains)
    application.features.tiedonsiirtomail || orgEnabled
  }
}
