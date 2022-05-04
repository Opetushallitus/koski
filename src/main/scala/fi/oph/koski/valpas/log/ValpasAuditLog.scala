package fi.oph.koski.valpas.log

import fi.oph.koski.log.{AuditLog, AuditLogMessage, AuditLogOperation, LogConfiguration}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.valpas.kansalainen.{KansalainenOppijatiedot, KansalaisnäkymänTiedot}
import fi.oph.koski.valpas.{ValpasHenkilöhakuResult, ValpasLöytyiHenkilöhakuResult}
import fi.oph.koski.valpas.log.ValpasOperation.ValpasOperation
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot, ValpasOppilaitos}
import fi.oph.koski.valpas.rouhinta.HeturouhinnanTulos
import fi.oph.koski.valpas.valpasrepository.{UusiOppivelvollisuudenKeskeytys, ValpasKuntailmoitusLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession

object ValpasAuditLog {
  def auditLogOppijaKatsominen(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Unit =
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.oppijaHenkilöOid -> oppijaOid)
    ))

  def auditLogOppilaitosKatsominen
    (oppilaitosOid: ValpasOppilaitos.Oid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.juuriOrganisaatio -> oppilaitosOid)
    ))
  }

  def auditLogKuntaKatsominen
    (kuntaOid: Organisaatio.Oid)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_KUNNAT_OPPIJAT_KATSOMINEN,
      session,
      Map(ValpasAuditLogMessageField.juuriOrganisaatio -> kuntaOid)
    ))
  }

  def auditLogOppijaKuntailmoitus
    (ilmoitus: ValpasKuntailmoitusLaajatTiedot)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KUNTAILMOITUS,
      session,
      Map(
        // oppijaOid:in olemassaolo on varmistettu validoinneissa jo aiemmin. Jos näin pitkälle on päästy ilman, niin
        // 500-erroriin johtava keskeytys get:n käytöstä on ok tapa käsitellä virhe.
        ValpasAuditLogMessageField.oppijaHenkilöOid -> ilmoitus.oppijaOid.get,
        ValpasAuditLogMessageField.ilmoittajaHenkilöOid -> ilmoitus.tekijä.henkilö.map(_.oid.toString).getOrElse(""),
        ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid -> ilmoitus.tekijä.organisaatio.oid,
        ValpasAuditLogMessageField.kohdeOrganisaatioOid -> ilmoitus.kunta.oid
      )
    ))
  }

  def auditLogHenkilöHaku
    (query: String)(henkilö: ValpasHenkilöhakuResult)(implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_HAKU,
      session,
      henkilö match {
        case tulos: ValpasLöytyiHenkilöhakuResult => Map(
          ValpasAuditLogMessageField.hakulause -> query,
          ValpasAuditLogMessageField.oppijaHenkilöOid -> tulos.oid,
        )
        case _ => Map(
          ValpasAuditLogMessageField.hakulause -> query,
        )
      }
    ))
  }

  def auditLogOppivelvollisuudenKeskeytys
    (keskeytys: UusiOppivelvollisuudenKeskeytys)
    (implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYS,
      session,
      Map(
        ValpasAuditLogMessageField.oppijaHenkilöOid -> keskeytys.oppijaOid,
        ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid -> keskeytys.tekijäOrganisaatioOid,
      )
    ))
  }

  def auditLogOppivelvollisuudenKeskeytysUpdate
    (oppijaOid: ValpasHenkilö.Oid, tekijäOrganisaatioOid: Organisaatio.Oid)
    (implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_MUOKKAUS,
      session,
      Map(
        ValpasAuditLogMessageField.oppijaHenkilöOid -> oppijaOid,
        ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid -> tekijäOrganisaatioOid,
      )
    ))
  }

  def auditLogOppivelvollisuudenKeskeytysDelete
    (oppijaOid: ValpasHenkilö.Oid, tekijäOrganisaatioOid: Organisaatio.Oid)
    (implicit session: ValpasSession)
  : Unit = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_POISTO,
      session,
      Map(
        ValpasAuditLogMessageField.oppijaHenkilöOid -> oppijaOid,
        ValpasAuditLogMessageField.ilmoittajaOrganisaatioOid -> tekijäOrganisaatioOid,
      )
    ))
  }

  def auditLogOppivelvollisuusrekisteriLuovutus(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Unit = {
    val message = ValpasAuditLogMessage(
      ValpasOperation.OPPIVELVOLLISUUSREKISTERI_LUOVUTUS,
      session,
      Map(ValpasAuditLogMessageField.oppijaHenkilöOid -> oppijaOid)
    )
    AuditLog.log(message)
  }

  // Logeilla on 16kB maksimikoko tällä hetkellä, joten logientry on jaettava. Käytännössä jokaiseen entryyn tulee
  // datan lisäksi myös 0.5-1kB muuta dataa.
  private val oidLengthInKoski = 26
  private val hetuLength = 11
  private val listSeparatorMaxLength = 2
  private val maxAuditLogMessageLength = LogConfiguration.logMessageMaxLength - 1024
  private val hetujaEnintäänAuditlogEntryssä = (maxAuditLogMessageLength / (hetuLength + listSeparatorMaxLength)).floor.toInt
  private val oidejaEnintäänAuditlogEntryssä = (maxAuditLogMessageLength / (oidLengthInKoski + listSeparatorMaxLength)).floor.toInt

  def auditLogRouhintahakuHetulistalla
    (hetut: Seq[String], palautetutOppijaOidit: Seq[String])
    (implicit session: ValpasSession)
  : Unit = {

    val hetuSivut = hetut.grouped(hetujaEnintäänAuditlogEntryssä).toList
    val palautetutOppijaOiditSivut = palautetutOppijaOidit.grouped(oidejaEnintäänAuditlogEntryssä).toList

    val sivuLukumäärä = hetuSivut.length + palautetutOppijaOiditSivut.length

    hetuSivut.zip(Stream.from(1)).foreach {
      case (hetut, sivu) =>
        AuditLog.log(ValpasAuditLogMessage(
          ValpasOperation.VALPAS_ROUHINTA_HETUHAKU,
          session,
          Map(
            ValpasAuditLogMessageField.hakulause -> hetut.mkString(", "),
            ValpasAuditLogMessageField.sivu -> s"${sivu}",
            ValpasAuditLogMessageField.sivuLukumäärä -> s"${sivuLukumäärä}"
          )
        ))
    }

    palautetutOppijaOiditSivut.zip(Stream.from(hetuSivut.length + 1)).foreach {
      case (oidit, sivu) =>
        AuditLog.log(ValpasAuditLogMessage(
          ValpasOperation.VALPAS_ROUHINTA_HETUHAKU,
          session,
          Map(
            ValpasAuditLogMessageField.oppijaHenkilöOidList -> oidit.mkString(" "),
            ValpasAuditLogMessageField.sivu -> s"${sivu}",
            ValpasAuditLogMessageField.sivuLukumäärä -> s"${sivuLukumäärä}"
          )
        ))
    }
  }

  def auditLogRouhintahakuKunnalla
    (kunta: String, palautetutOppijaOidit: Seq[String])
    (implicit session: ValpasSession)
  : Unit = {

    val palautetutOppijaOiditSivut = palautetutOppijaOidit.grouped(oidejaEnintäänAuditlogEntryssä).toList
    val sivuLukumäärä = palautetutOppijaOiditSivut.length

    palautetutOppijaOiditSivut.zip(Stream.from(1)).foreach {
      case (oidit, sivu) =>
        AuditLog.log(ValpasAuditLogMessage(
          ValpasOperation.VALPAS_ROUHINTA_KUNTA,
          session,
          Map(
            ValpasAuditLogMessageField.hakulause -> kunta,
            ValpasAuditLogMessageField.oppijaHenkilöOidList -> oidit.mkString(" "),
            ValpasAuditLogMessageField.sivu -> s"${sivu}",
            ValpasAuditLogMessageField.sivuLukumäärä -> s"${sivuLukumäärä}"
          )
        ))
    }
  }

  def auditLogKansalainenOmatTiedot
    (tiedot: KansalaisnäkymänTiedot)
    (implicit session: ValpasSession)
  : Unit =
    logKansalainenKatsominen(
      tiedot.omatTiedot.toList,
      ValpasOperation.VALPAS_KANSALAINEN_KATSOMINEN
    )

  def auditLogKansalainenHuollettavienTiedot
    (tiedot: KansalaisnäkymänTiedot)
    (implicit session: ValpasSession)
  : Unit =
    logKansalainenKatsominen(
      tiedot.huollettavat,
      ValpasOperation.VALPAS_KANSALAINEN_HUOLTAJA_KATSOMINEN
    )

  private def logKansalainenKatsominen
    (tiedot: Seq[KansalainenOppijatiedot], operation: ValpasOperation)
    (implicit session: ValpasSession)
  : Unit = {
    if (tiedot.nonEmpty) {
      val oppijaOidit = tiedot.map(_.oppija.henkilö.oid)
      val message = ValpasAuditLogMessage(
        operation,
        session,
        Map(ValpasAuditLogMessageField.oppijaHenkilöOidList -> oppijaOidit.mkString(" ")),
      )
      AuditLog.log(message)
    }
  }

}

object ValpasAuditLogMessage {
  def apply(operation: ValpasOperation, session: ValpasSession, extraFields: AuditLogMessage.ExtraFields): AuditLogMessage = {
    AuditLogMessage(new ValpasAuditLogOperation(operation), session, extraFields)
  }
}

object ValpasAuditLogMessageField extends Enumeration {
  type ValpasAuditLogMessageField = Value
  val oppijaHenkilöOid,
      oppijaHenkilöOidList,
      juuriOrganisaatio,
      ilmoittajaHenkilöOid,
      ilmoittajaOrganisaatioOid,
      kohdeOrganisaatioOid,
      hakulause,
      hakutulosOppijaOid,
      sivu,
      sivuLukumäärä = Value
}

object ValpasOperation extends Enumeration {
  type ValpasOperation = Value
  val VALPAS_OPPIJA_KATSOMINEN,
      VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
      VALPAS_KUNNAT_OPPIJAT_KATSOMINEN,
      VALPAS_OPPIJA_KUNTAILMOITUS,
      VALPAS_OPPIJA_HAKU,
      VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYS,
      VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_MUOKKAUS,
      VALPAS_OPPIVELVOLLISUUDEN_KESKEYTYKSEN_POISTO,
      OPPIVELVOLLISUUSREKISTERI_LUOVUTUS,
      VALPAS_ROUHINTA_HETUHAKU,
      VALPAS_ROUHINTA_KUNTA,
      VALPAS_KANSALAINEN_KATSOMINEN,
      VALPAS_KANSALAINEN_HUOLTAJA_KATSOMINEN = Value
}

private class ValpasAuditLogOperation(op: ValpasOperation) extends AuditLogOperation(op)
