package fi.oph.koski.raportointikanta

import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{OrganisaatioPalveluOrganisaatio, OrganisaatioRepository}
import fi.oph.koski.schema.LocalizedString

object OrganisaatioLoader extends Logging {
  private val BatchSize = 1000
  private val name = "organisaatiot"

  def loadOrganisaatiot(organisaatioRepository: OrganisaatioRepository, db: RaportointiDatabase): Int = {
    logger.info("Ladataan organisaatioita...")
    val organisaatiot = organisaatioRepository.findAllRaw
    logger.info(s"Löytyi ${organisaatiot.size} organisaatioita")
    db.setStatusLoadStarted(name)
    db.deleteOrganisaatiot
    db.deleteOrganisaatioKielet
    val count = organisaatiot.grouped(BatchSize).map(batch => {
      val batchRows = batch.map(buildROrganisaatioRow)
      val organisaatioRows = batchRows.map(_._1)
      val kieletRows = batchRows.map(_._2).flatten

      db.loadOrganisaatiot(organisaatioRows)
      db.loadOrganisaatioKielet(kieletRows)
      db.setLastUpdate(name)
      batchRows.size
    }).sum
    db.setStatusLoadCompletedAndCount(name, count)
    logger.info(s"Ladattiin $count ${name}a")
    count
  }

  private def buildROrganisaatioRow(org: OrganisaatioPalveluOrganisaatio): Tuple2[ROrganisaatioRow, Seq[ROrganisaatioKieliRow]] = {
    val oid = org.oid
    val organisaatioRow = ROrganisaatioRow(
      organisaatioOid = oid,
      nimi = LocalizedString.sanitizeRequired(org.nimi, org.oid).get("fi"),
      organisaatiotyypit = org.organisaatiotyypit.sorted.mkString(","),
      oppilaitostyyppi = org.oppilaitostyyppi.map(_.split('#').head.split('_').last),
      oppilaitosnumero = org.oppilaitosKoodi,
      kotipaikka = org.kotipaikkaUri.map(_.stripPrefix("kunta_")),
      yTunnus = org.ytunnus
    )
    val organisaatioKieliRows = org.kieletUris.map(ROrganisaatioKieliRow(oid, _))
    (organisaatioRow, organisaatioKieliRows)
  }
}
