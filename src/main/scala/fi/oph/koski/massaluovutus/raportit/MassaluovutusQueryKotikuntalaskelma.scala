package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Session}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{OppilaitosRaporttiResponse, RaportitAccessResolver, RaportitService, RaporttiPäivältäRequest}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("Kotikuntalaskelma")
@Description("Palauttaa perusopetusikäisten oppilaiden lukumäärät opetuksen järjestäjittäin ja oppilaan kotikunnittain valittuna päivänä. Kysely vaatii valitussa organisaatiossa oikeudet sekä perusopetuksen että esiopetuksen opiskeluoikeuksiin.")
case class MassaluovutusQueryKotikuntalaskelma(
  @EnumValues(Set("kotikuntalaskelma"))
  `type`: String = "kotikuntalaskelma",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan oid.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Päivämäärä, jolta raportti lasketaan. Oppilaiden kotikunta valitaan samalta päivältä raporttia varten.")
  paiva: LocalDate,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryKotikuntalaskelma] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetus
  override def raporttiName = "kotikuntalaskelma"

  // Sama ehto kuin RaportitServletissä: raportilla on oppijoita kaikista sen koulutusmuodoista
  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean =
    super.queryAllowed(application) && withKoskiSpecificSession { u =>
      organisaatioOid.exists(oid =>
        RaportitAccessResolver(application).hasKaikkiOpiskeluoikeudenTyypit(oid, RaportitAccessResolver.kotikuntalaskelmanOpiskeluoikeudenTyypit)(u)
      )
    }

  override def auditLogParams = Map(
    "paiva" -> List(paiva.format(DateTimeFormatter.ISO_DATE)),
  )

  override protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val request = RaporttiPäivältäRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      paiva = paiva,
      lang = language.get,
    )
    raportitService.kotikuntalaskelma(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryKotikuntalaskelmaDocumentation {
  def xlsxExample: MassaluovutusQueryKotikuntalaskelma = MassaluovutusQueryKotikuntalaskelma(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    language = Some("fi"),
    paiva = LocalDate.of(2026, 12, 31),
    password = Some("hunter2"),
  )
}
