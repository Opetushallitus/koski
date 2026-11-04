package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiOperation.{KoskiOperation, SISALTYVAT_OPISKELUOIKEUDET_HAKU}
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.organisaatio.{MockOrganisaatiot, OrganisaationAlaisetOrganisaatiot}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}
import slick.jdbc.SQLActionBuilder

import java.time.{LocalDate, LocalDateTime}

@Title("Organisaation opiskeluoikeuksiin sisältyvät opiskeluoikeudet")
@Description("Palauttaa muiden oppilaitosten opiskeluoikeudet, jotka on merkitty sisältyviksi (kenttä sisältyyOpiskeluoikeuteen) organisaation tai sen alaorganisaatioiden opiskeluoikeuksiin. Täydentää organisaationOpiskeluoikeudet-kyselyä: sisältyvyys on merkitty vain sisältyvään opiskeluoikeuteen, joten sisältävän opiskeluoikeuden oppilaitos ei muuten näe, mitkä muualle kirjatut opiskeluoikeudet on liitetty sen omiin opiskeluoikeuksiin.")
@Description("Hakuehdot kohdistuvat palautettaviin eli sisältyviin opiskeluoikeuksiin, eivät organisaation omiin sisältäviin opiskeluoikeuksiin. Opiskeluoikeus palautetaan vain, jos sen sisältävä opiskeluoikeus on olemassa eikä sitä ole mitätöity. Kunkin palautetun opiskeluoikeuden kenttä sisältyyOpiskeluoikeuteen kertoo, mihin organisaation opiskeluoikeuteen se on liitetty.")
@Description("Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.")
case class MassaluovutusQuerySisaltyvatOpiskeluoikeudet(
  @EnumValues(Set("sisaltyvatOpiskeluoikeudet"))
  `type`: String = "sisaltyvatOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  organisaatioOid: Option[Organisaatio.Oid] = None,
  alkanutAikaisintaan: LocalDate,
  alkanutViimeistään: Option[LocalDate] = None,
  päättynytAikaisintaan: Option[LocalDate] = None,
  päättynytViimeistään: Option[LocalDate] = None,
  eiPäättymispäivää: Option[Boolean] = None,
  muuttunutJälkeen: Option[LocalDateTime] = None,
  koulutusmuoto: Option[String] = None,
  mitätöidyt: Option[Boolean] = None,
) extends MassaluovutusQueryOrganisaationOpiskeluoikeudetBase {
  def withOrganisaatioOid(organisaatioOid: Oid): MassaluovutusQuerySisaltyvatOpiskeluoikeudet = copy(organisaatioOid = Some(organisaatioOid))

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    organisaatiot: OrganisaationAlaisetOrganisaatiot,
  )(implicit user: KoskiSpecificSession): Either[String, Unit] =
    fetchDataAsJson(application, writer, organisaatiot)

  // Sisältävän opiskeluoikeuden oppilaitoksella on lukuoikeus sisältyvään opiskeluoikeuteen saman sarakkeen
  // perusteella (ks. KoskiTables.KoskiOpiskeluOikeudetWithAccessCheck). Ostopalveluyksiköitä ei huomioida,
  // koska lukuoikeuskaan ei ulotu niiden kautta sisältyviin opiskeluoikeuksiin.
  protected def organisaatioFilter(organisaatiot: OrganisaationAlaisetOrganisaatiot): SQLActionBuilder =
    sql"""
      AND sisaltava_opiskeluoikeus_oppilaitos_oid = ANY(${organisaatiot.hierarkia})
      AND EXISTS (
        SELECT 1 FROM opiskeluoikeus sisaltava
        WHERE sisaltava.oid = opiskeluoikeus.sisaltava_opiskeluoikeus_oid
          AND NOT sisaltava.poistettu
          AND NOT sisaltava.mitatoity
      )
    """

  override protected def auditLogOperation: KoskiOperation = SISALTYVAT_OPISKELUOIKEUDET_HAKU
}

object QuerySisaltyvatOpiskeluoikeudetDocumentation {
  def example: MassaluovutusQuerySisaltyvatOpiskeluoikeudet = MassaluovutusQuerySisaltyvatOpiskeluoikeudet(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    alkanutAikaisintaan = LocalDate.of(2024, 1, 1),
  )
}
