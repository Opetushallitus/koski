package fi.oph.koski.massaluovutus.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.MassaluovutusQueryParameters
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.massaluovutus.ValpasMassaluovutusOppija
import fi.oph.koski.valpas.oppija.ValpasAccessResolver
import fi.oph.koski.valpas.valpasuser.ValpasSession

trait ValpasMassaluovutusQueryParameters extends MassaluovutusQueryParameters with Logging with Timing {
  val sivukoko = 1000
  def kuntaOid: String

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = user match {
    case valpasSession: ValpasSession =>
      val kuntaOpt = getKuntaKoodiByKuntaOid(application, kuntaOid)
      val accessResolver = new ValpasAccessResolver
      val hasAccess = accessResolver.accessToMassaluovutusrajapintaKuntaOrg(kuntaOid)(valpasSession)
      kuntaOpt.nonEmpty && hasAccess
    case _ => false
  }

  protected def getKuntaKoodiByKuntaOid(application: KoskiApplication, kuntaOid: String): Option[String] = {
    Kunta.validateAndGetKuntaKoodi(application.organisaatioService, application.koodistoPalvelu, kuntaOid) match {
      case Right(kuntaKoodi) => Some(kuntaKoodi)
      case Left(error) =>
        logger.warn(s"ValpasEiOppivelvollisuuttaSuorittavatQuery getKuntaKoodiByKuntaOid: ${error.errors.map(_.toString).mkString(", ")}")
        None
    }
  }

  def withOppivelvollisuustiedot(
    oppijat: Seq[ValpasMassaluovutusOppija],
    application: KoskiApplication
  ): Seq[ValpasMassaluovutusOppija] = {
    timed("ValpasMassaluovutusQueryParameters:withOppivelvollisuustiedot") {
      val oppijaOids = oppijat.map(_.oppijanumero)
      val oppivelvollisuustiedot = Oppivelvollisuustiedot.queryByOids(
        oppijaOids,
        application.raportointiDatabase
      )
      val oppivelvollisuustiedotByOid = oppivelvollisuustiedot.map(t => t.oid -> t).toMap

      oppijat.map { oppija =>
        val oikeusMaksuttomuuteenPäättyy = oppivelvollisuustiedotByOid
          .get(oppija.oppijanumero)
          .map(_.oikeusMaksuttomaanKoulutukseenVoimassaAsti)
        val kotikuntaSuomessaAlkaen = oppivelvollisuustiedotByOid
          .get(oppija.oppijanumero)
          .map(_.kotikuntaSuomessaAlkaen)

        oppija.copy(
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = oikeusMaksuttomuuteenPäättyy,
          kotikuntaSuomessaAlkaen = kotikuntaSuomessaAlkaen
        )
      }
    }
  }
}
