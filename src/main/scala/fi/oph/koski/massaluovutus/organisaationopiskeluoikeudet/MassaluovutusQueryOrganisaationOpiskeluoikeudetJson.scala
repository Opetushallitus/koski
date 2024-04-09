package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, KoskiTables}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.MassaluovutusUtils.QueryResourceManager
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema, Oppija, Organisaatio}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("(JSON)")
@Description("Tulostiedostot sisältävät tiedot json-muodossa. Jokaista oppijaa kohden luodaan oma tiedostonsa, jonka alle opiskeluoikeudet on ryhmitelty.")
@Description("Tiedostojen sisältö vastaa opintohallintojärjestelmille tarkoitettua rajapintaa GET /koski/api/oppija/{oid}")
case class MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(
  `type`: String = "organisaationOpiskeluoikeudet",
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
) extends MassaluovutusQueryOrganisaationOpiskeluoikeudet {
  def withOrganisaatioOid(organisaatioOid: Oid): MassaluovutusQueryOrganisaationOpiskeluoikeudetJson = copy(organisaatioOid = Some(organisaatioOid))

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  )(implicit user: KoskiSpecificSession): Either[String, Unit] = QueryResourceManager(logger) { _ =>
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    forEachOpiskeluoikeusAndHenkilö(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      writer.putJson(henkilö.oid, Oppija(
        henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
        opiskeluoikeudet = opiskeluoikeudet.flatMap(toKoskeenTallennettavaOpiskeluoikeus(application)),
      ))
    }
  }

  private def toKoskeenTallennettavaOpiskeluoikeus(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[KoskeenTallennettavaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => Some(oo)
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }
}

object QueryOrganisaationOpiskeluoikeudetJsonDocumentation {
  def example = MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    alkanutAikaisintaan = LocalDate.of(2024, 1, 1),
    alkanutViimeistään = Some(LocalDate.of(2024, 1, 31)),
    koulutusmuoto = Some("perusopetus"),
  )
}
