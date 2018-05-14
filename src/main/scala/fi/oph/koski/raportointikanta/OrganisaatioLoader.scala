package fi.oph.koski.raportointikanta

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{OrganisaatioPalveluOrganisaatio, OrganisaatioRepository}

object OrganisaatioLoader extends Logging {
  private val BatchSize = 1000

  def loadOrganisaatiot(organisaatioRepository: OrganisaatioRepository, raportointiDatabase: RaportointiDatabase): Int = {
    logger.info("Ladataan organisaatioita...")
    val organisaatiot = organisaatioRepository.findAllRaw
    logger.info(s"Löytyi ${organisaatiot.size} organisaatioita")
    raportointiDatabase.deleteOrganisaatiot
    val count = organisaatiot.grouped(BatchSize).map(batch => {
      val batchRows = batch.map(buildROrganisaatioRow)
      raportointiDatabase.loadOrganisaatiot(batchRows)
      batchRows.size
    }).sum
    logger.info(s"Ladattiin $count organisaatiota")
    count
  }

  private def buildROrganisaatioRow(org: OrganisaatioPalveluOrganisaatio) =
    ROrganisaatioRow(
      organisaatioOid = org.oid,
      nimi = LocalizedString.sanitizeRequired(org.nimi, org.oid).get("fi"),
      organisaatiotyypit = org.organisaatiotyypit.mkString(","),
      oppilaitostyyppi = org.oppilaitostyyppi.map(_.split('#').head.split('_').last),
      oppilaitosnumero = org.oppilaitosKoodi
    )
}
