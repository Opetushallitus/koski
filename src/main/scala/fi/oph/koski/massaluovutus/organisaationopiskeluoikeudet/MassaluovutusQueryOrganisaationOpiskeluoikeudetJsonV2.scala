package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, KoskiTables}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.MassaluovutusUtils.QueryResourceManager
import fi.oph.koski.massaluovutus.{MassaluovutusException, QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{Henkilö, KoskeenTallennettavaOpiskeluoikeus, KoskiSchema, Organisaatio, TäydellisetHenkilötiedot}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("(JSON V2)")
@Description("Tulostiedostot sisältävät tiedot json-muodossa. Jokaista master-oppijaa kohden luodaan oma tiedostonsa, jonka alle kaikkien linkitettyjen oppijoiden opiskeluoikeudet on ryhmitelty.")
@Description("V2-versiossa tulostiedostot on ryhmitelty master-oidin mukaan ja sisältävät muutHenkilöOidit-kentän oppijaMasterOid-kentän sijasta.")
case class MassaluovutusQueryOrganisaationOpiskeluoikeudetJsonV2(
  @EnumValues(Set("organisaationOpiskeluoikeudetV2"))
  `type`: String = "organisaationOpiskeluoikeudetV2",
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
  def withOrganisaatioOid(organisaatioOid: Oid): MassaluovutusQueryOrganisaationOpiskeluoikeudetJsonV2 = copy(organisaatioOid = Some(organisaatioOid))

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  )(implicit user: KoskiSpecificSession): Either[String, Unit] = QueryResourceManager(logger) { _ =>
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    case class OppijaAcc(
      henkilö: LaajatOppijaHenkilöTiedot,
      muutHenkilöOidit: Set[Henkilö.Oid],
      opiskeluoikeudet: Map[String, KoskeenTallennettavaOpiskeluoikeus],
    )

    var grouped = Map.empty[Henkilö.Oid, OppijaAcc]

    forEachOpiskeluoikeusAndHenkilö(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      val masterHenkilö = application.opintopolkuHenkilöFacade.findMasterOppija(henkilö.oid).getOrElse(henkilö)
      val masterOid = masterHenkilö.oid
      val deserialized = opiskeluoikeudet.flatMap(toKoskeenTallennettavaOpiskeluoikeus(application))
      val newOos = deserialized.flatMap(oo => oo.oid.map(_ -> oo)).toMap
      val otherOid = Option.when(henkilö.oid != masterOid)(henkilö.oid).toSet

      grouped = grouped.updatedWith(masterOid) {
        case Some(acc) => Some(acc.copy(
          muutHenkilöOidit = acc.muutHenkilöOidit ++ otherOid,
          opiskeluoikeudet = acc.opiskeluoikeudet ++ newOos,
        ))
        case None => Some(OppijaAcc(
          henkilö = masterHenkilö,
          muutHenkilöOidit = otherOid,
          opiskeluoikeudet = newOos,
        ))
      }
    }

    grouped.foreach { case (masterOid, acc) =>
      writer.putJson(masterOid, MassaluovutusOppijaV2(
        henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(acc.henkilö),
        muutHenkilöOidit = acc.muutHenkilöOidit.toSeq.sorted,
        opiskeluoikeudet = acc.opiskeluoikeudet.values.toSeq,
      ))
    }
  }

  private def toKoskeenTallennettavaOpiskeluoikeus(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[KoskeenTallennettavaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => Some(oo)
      case Left(errors) =>
        logger.warn(s"Error deserializing oppijan ${row.oppijaOid} opiskeluoikeus ${row.oid}: ${errors}")
        throw new MassaluovutusException(s"Oppijan ${row.oppijaOid} opiskeluoikeuden ${row.oid} deserialisointi epäonnistui")
    }
  }
}

case class MassaluovutusOppijaV2(
  henkilö: TäydellisetHenkilötiedot,
  muutHenkilöOidit: Seq[Henkilö.Oid],
  opiskeluoikeudet: Seq[KoskeenTallennettavaOpiskeluoikeus],
)

object QueryOrganisaationOpiskeluoikeudetJsonV2Documentation {
  def example = MassaluovutusQueryOrganisaationOpiskeluoikeudetJsonV2(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    alkanutAikaisintaan = LocalDate.of(2024, 1, 1),
    alkanutViimeistään = Some(LocalDate.of(2024, 1, 31)),
    koulutusmuoto = Some("perusopetus"),
  )
}
