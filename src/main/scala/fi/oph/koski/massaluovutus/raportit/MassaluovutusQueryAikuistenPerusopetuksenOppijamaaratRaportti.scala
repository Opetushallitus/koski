package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{OppilaitosRaporttiResponse, RaportitService, RaporttiPäivältäRequest}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("Aikuisten perusopetuksen oppijamäärät")
@Description("Palauttaa aikuisten perusopetuksen oppijamäärät-raportin.")
case class MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(
  @EnumValues(Set("aikuistenPerusopetuksenOppijamaaratRaportti"))
  `type`: String = "aikuistenPerusopetuksenOppijamaaratRaportti",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Päivämäärä, jolta oppijamäärät lasketaan.")
  paiva: LocalDate,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.aikuistenperusopetus
  override def raporttiName = "aikuistenperusopetuksenoppijamaaratraportti"

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
    raportitService.aikuistenperusopetuksenOppijamäärät(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryAikuistenPerusopetuksenOppijamaaratRaporttiDocumentation {
  def xlsxExample: MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti = MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    language = Some("fi"),
    paiva = LocalDate.of(2024, 1, 15),
    password = Some("hunter2"),
  )
}
