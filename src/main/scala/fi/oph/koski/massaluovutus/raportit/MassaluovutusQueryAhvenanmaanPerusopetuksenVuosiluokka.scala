package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.{OppilaitosRaporttiResponse, PerusopetuksenVuosiluokkaRequest, RaportitService}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("Ahvenanmaan perusopetuksen vuosiluokka")
@Description("Palauttaa Ahvenanmaan perusopetuksen vuosiluokkaraportin.")
case class MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka(
  @EnumValues(Set("ahvenanmaanPerusopetuksenVuosiluokka"))
  `type`: String = "ahvenanmaanPerusopetuksenVuosiluokka",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Päivämäärä, jolta raportti lasketaan.")
  paiva: LocalDate,
  @Description("Vuosiluokka (1-9).")
  vuosiluokka: String,
  @Description("Kotikuntapäivämäärä historialliseen kotikuntahakuun.")
  kotikuntaPvm: Option[LocalDate] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ahvenanmaanperusopetus
  override def raporttiName = "ahvenanmaanperusopetuksenvuosiluokka"

  override def auditLogParams = Map(
    "paiva" -> List(paiva.format(DateTimeFormatter.ISO_DATE)),
    "vuosiluokka" -> List(vuosiluokka),
    "kotikuntaPvm" -> kotikuntaPvm.map(_.format(DateTimeFormatter.ISO_DATE)).toList,
  )

  override protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val request = PerusopetuksenVuosiluokkaRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      paiva = paiva,
      vuosiluokka = vuosiluokka,
      lang = language.get,
      kotikuntaPvm = kotikuntaPvm,
    )
    raportitService.ahvenanmaanPerusopetuksenVuosiluokka(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryAhvenanmaanPerusopetuksenVuosiluokkaDocumentation {
  def xlsxExample: MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka = MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.övernäsSkola),
    language = Some("sv"),
    paiva = LocalDate.of(2026, 5, 15),
    vuosiluokka = "9",
    password = Some("hunter2"),
  )
}
