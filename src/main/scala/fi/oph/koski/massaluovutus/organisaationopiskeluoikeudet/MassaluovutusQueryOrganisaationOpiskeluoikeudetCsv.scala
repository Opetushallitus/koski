package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.MassaluovutusUtils.QueryResourceManager
import fi.oph.koski.massaluovutus.{CsvStream, PartitionSupport, QueryFormat, QueryResultWriter}
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}
import scala.language.implicitConversions
import scala.util.Using


@Title("(CSV)")
@Description("Tulostiedostot sisältävät tiedot csv-muodossa. Tiedostoa ei luoda, jos se jää sisällöltään tyhjäksi.")
@Description("Tiedostojen skeema vastaa KOSKI-raportointikannan skeemaa, joten integraatiossa on hyvä huomioida sen mahdollinen muuttuminen.")
@Description("Skeema erittäin harvoin muuttuu niin, että kenttiä poistetaan tai niiden muoto muuttuu, mutta uusia kenttiä voi tulla mukaan.")
@Description("Huom! Taulujen väliset relaatiot eivät ole stabiileja kyselyiden välillä, vaan id-kentät ovat kyselykohtaisia.")
@Description("Jos kyselyn tuloksena syntyvä tulostiedosto on liian iso (n. 5 gigatavua), kysely epäonnistuu.")
@Description("Tällaisessa tilanteessa tee kysely lyhyemmälle aikavälille tai käytä formaattinen text/x-csv-partition,")
@Description("jolloin tiedostot jaetaan useampaan palaseen ja tiedostonimissä on mukana palasen numero.")
@Description("Käyttäjän vastuulle jää tiedostojen yhdistäminen.")
@Description(QueryOrganisaationOpiskeluoikeudetCsvDocumentation.fileDescriptionsAsHtml)
case class MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.csv, QueryFormat.csvPartition))
  format: String = QueryFormat.csv,
  organisaatioOid: Option[Organisaatio.Oid] = None,
  alkanutAikaisintaan: LocalDate,
  alkanutViimeistään: Option[LocalDate] = None,
  päättynytAikaisintaan: Option[LocalDate] = None,
  päättynytViimeistään: Option[LocalDate] = None,
  eiPäättymispäivää: Option[Boolean] = None,
  muuttunutJälkeen: Option[LocalDateTime] = None,
  koulutusmuoto: Option[String] = None,
  mitätöidyt: Option[Boolean] = None,
  @Description("Jos true, ei kyselyllä luoda osasuoritustiedostoja")
  eiOsasuorituksia: Option[Boolean] = None,
  @Description("Jos true, ei kyselyllä luoda opiskeluoikeuden aikajaksotiedostoja")
  eiAikajaksoja: Option[Boolean] = None,
) extends MassaluovutusQueryOrganisaationOpiskeluoikeudet with PartitionSupport {
  def partitionFormats: Seq[String] = Seq(QueryFormat.csvPartition)

  def withOrganisaatioOid(organisaatioOid: Oid): MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv = copy(organisaatioOid = Some(organisaatioOid))

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  )(implicit user: KoskiSpecificSession): Either[String, Unit] = QueryResourceManager(logger) { mgr =>
    implicit val manager: Using.Manager = mgr

    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    val partitionSize = if (format == QueryFormat.csvPartition) Some(QueryResultWriter.defaultPartitionSize(application.config)) else None
    val opiskeluoikeusCsv = CsvResultFile.opiskeluoikeudet.create(writer, partitionSize)
    val päätasonSuoritusCsv = CsvResultFile.päätasonSuoritukset.create(writer, partitionSize)
    val osasuoritusCsv = CsvResultFile.osasuoritukset.create(writer, partitionSize)
    val opiskeluoikeudenAikajaksoCsv = CsvResultFile.opiskeluoikeudenAikajaksot.create(writer, partitionSize)
    val esiopetuksenAikajaksoCsv = CsvResultFile.esiopetuksenOpiskeluoikeudenAikajaksot.create(writer, partitionSize)
    val mitätöityOpiskeluoikeusCsv = CsvResultFile.mitätöidytOpiskeluoikeudet.create(writer, partitionSize)

    val includeAikajaksot = !eiAikajaksoja.contains(true)
    val includeOsasuoritukset = !eiOsasuorituksia.contains(true)

    forEachOpiskeluoikeus(application, filters, oppijaOids) { row =>
      if (row.mitätöity) {
        OpiskeluoikeusLoaderRowBuilder
          .buildRowMitätöity(row)
          .foreach(mitätöityOpiskeluoikeusCsv.put)
      } else {
        OpiskeluoikeusLoaderRowBuilder
          .buildKoskiRow(row, includeAikajaksot, includeOsasuoritukset)
          .foreach { rows =>
            opiskeluoikeusCsv.put(rows.rOpiskeluoikeusRow)
            rows.rPäätasonSuoritusRows.foreach(row => päätasonSuoritusCsv.put(new CsvPäätasonSuoritus(row)))
            if (includeOsasuoritukset) {
              rows.rOsasuoritusRows.foreach(row => osasuoritusCsv.put(new CsvOsauoritus(row)))
            }
            if (includeAikajaksot) {
              opiskeluoikeudenAikajaksoCsv.put(rows.rOpiskeluoikeusAikajaksoRows)
              esiopetuksenAikajaksoCsv.put(rows.esiopetusOpiskeluoikeusAikajaksoRows)
            }
          }
      }
    }

    opiskeluoikeusCsv.save()
    päätasonSuoritusCsv.save()
    osasuoritusCsv.save()
    opiskeluoikeudenAikajaksoCsv.save()
    esiopetuksenAikajaksoCsv.save()
    mitätöityOpiskeluoikeusCsv.save()
  }
}

object CsvResultFile extends Enumeration {
  type CsvResultFile = Value
  protected case class FileDescription[T <: Product](
    name: String,
    title: String,
    schemaUrl: String,
  ) extends super.Val {
    def fullName: String = s"$name.csv"
    def create(writer: QueryResultWriter, partitionSize: Option[Long] = None)(implicit manager: Using.Manager): CsvStream[T] =
      writer.createCsv[T](name, partitionSize)
  }

  implicit def valueToFileDescription(x: Value): FileDescription[_] = x.asInstanceOf[FileDescription[_]]

  val opiskeluoikeudet: FileDescription[ROpiskeluoikeusRow] = FileDescription(
    "opiskeluoikeus",
    "Opiskeluoikeudet",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_opiskeluoikeus.html",
  )
  val päätasonSuoritukset: FileDescription[RPäätasonSuoritusRow] = FileDescription(
    "paatason_suoritus",
    "Päätason suoritukset",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_paatason_suoritus.html"
  )
  val osasuoritukset: FileDescription[ROsasuoritusRow] = FileDescription(
    "osasuoritus",
    "Osasuoritukset",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_osasuoritus.html"
  )
  val opiskeluoikeudenAikajaksot: FileDescription[ROpiskeluoikeusAikajaksoRow] = FileDescription(
    "opiskeluoikeus_aikajakso",
    "Opiskeluoikeuksien aikajaksot",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_opiskeluoikeus_aikajakso.html",
  )
  val esiopetuksenOpiskeluoikeudenAikajaksot: FileDescription[EsiopetusOpiskeluoikeusAikajaksoRow] = FileDescription(
    "esiopetus_opiskeluoik_aikajakso",
    "Esiopetuksen opiskeluoikeuksien aikajaksot",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/esiopetus_opiskeluoik_aikajakso.html",
  )
  val mitätöidytOpiskeluoikeudet: FileDescription[RMitätöityOpiskeluoikeusRow] = FileDescription(
    "mitatoity_opiskeluoikeus",
    "Mitätöidyt opiskeluoikeudet",
    "https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_mitatoitu_opiskeluoikeus.html",
  )
}

object QueryOrganisaationOpiskeluoikeudetCsvDocumentation {
  val fileDescriptionsAsHtml: String =
    "<ul>" +
      CsvResultFile.values
        .toList
        .map(d => s"<li>${d.fullName} (<a href=" + '"' + d.schemaUrl + '"' + s">${d.title}</a>)</li>")
        .mkString("\n") +
    "</ul>"

  def outputFiles: List[String] = CsvResultFile.values.map(_.name).toList

  def example: MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv = MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    alkanutAikaisintaan = LocalDate.of(2024, 1, 1),
    alkanutViimeistään = Some(LocalDate.of(2024, 1, 31)),
    koulutusmuoto = Some("perusopetus"),
  )
}
