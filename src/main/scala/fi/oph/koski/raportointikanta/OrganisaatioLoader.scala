package fi.oph.koski.raportointikanta

import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioPalveluOrganisaatio, OrganisaatioRepository, Organisaatiotyyppi}
import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.{LocalizedString, OrganisaatioWithOid}

object OrganisaatioLoader extends Logging {
  private val BatchSize = 1000
  private val name = "organisaatiot"

  def loadOrganisaatiot(organisaatioRepository: OrganisaatioRepository, db: RaportointiDatabase): Int = {
    logger.info("Ladataan organisaatioita...")
    val organisaatiopuu = organisaatioRepository.findAllHierarkiat
    val organisaatiot = OrganisaatioHierarkia.flatten(organisaatiopuu)
    logger.info(s"Löytyi ${organisaatiot.size} organisaatioita")

    db.setStatusLoadStarted(name)
    val count = organisaatiot.grouped(BatchSize).map(batch => {
      val batchRows = batch.map(buildROrganisaatioRow)
      val organisaatioRows = batchRows.map(_._1)
      val kieletRows = batchRows.flatMap(_._2)

      db.loadOrganisaatiot(organisaatioRows)
      db.loadOrganisaatioKielet(kieletRows)
      db.setLastUpdate(name)
      batchRows.size
    }).sum
    db.setStatusLoadCompletedAndCount(name, count)
    logger.info(s"Ladattiin $count ${name}a")
    count
  }

  private def buildROrganisaatioRow(org: OrganisaatioHierarkia): (ROrganisaatioRow, Seq[ROrganisaatioKieliRow]) = {
    val oid = org.oid
    val organisaatioRow = ROrganisaatioRow(
      organisaatioOid = oid,
      nimi = org.nimi.get("fi"),
      nimiSv = org.nimi.get("sv"),
      organisaatiotyypit = org.organisaatiotyypit.sorted.mkString(","),
      oppilaitostyyppi = org.oppilaitostyyppi.map(_.split('#').head.split('_').last),
      oppilaitosnumero = org.oppilaitosnumero.map(_.koodiarvo),
      kotipaikka = org.kotipaikka.map(_.koodiarvo),
      yTunnus = org.yTunnus,
      koulutustoimija = koulutustoimijaOf(org),
      oppilaitos = oppilaitosOf(org),
    )
    val organisaatioKieliRows = org.kielikoodit.map(ROrganisaatioKieliRow(oid, _))
    (organisaatioRow, organisaatioKieliRows)
  }

  private def koulutustoimijaOf(org: OrganisaatioHierarkia): Option[String] = {
    val lapsityypit = List(
      Organisaatiotyyppi.OPPILAITOS,
      Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA,
      Organisaatiotyyppi.OPPISOPIMUSTOIMIPISTE,
    )
    val lapsenlapsityypit = List(Organisaatiotyyppi.TOIMIPISTE)

    if (org.organisaatiotyypit.exists(lapsityypit.contains)) {
      org.parent
    } else if (org.organisaatiotyypit.exists(lapsenlapsityypit.contains)) {
      org.grandparent
    } else {
      None
    }
  }

  private def oppilaitosOf(org: OrganisaatioHierarkia): Option[String] =
    if (org.organisaatiotyypit.contains(Organisaatiotyyppi.TOIMIPISTE)) {
      org.parent
    } else {
      None
    }
}
