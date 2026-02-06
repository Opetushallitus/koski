package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{AikajaksoRaporttiAikarajauksellaRequest, OppilaitosRaporttiResponse, RaportitService}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("Lukio 2019 suoritustietojen tarkistus")
@Description("Palauttaa lukion 2019 suoritustietojen tarkistusraportin (LOPS 2021).")
case class MassaluovutusQueryLukio2019Suoritustiedot(
  @EnumValues(Set("lukio2019Suoritustiedot"))
  `type`: String = "lukio2019Suoritustiedot",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Tutkittavan aikajakson alkamispäivä.")
  alku: LocalDate,
  @Description("Tutkittavan aikajakson päättymispäivä.")
  loppu: LocalDate,
  @Description("Jos true, osasuorituksiin rajoitetaan vain aikajakson sisällä arvioidut.")
  osasuoritustenAikarajaus: Option[Boolean] = None,
  @Description("Kotikuntapäivämäärä historialliseen kotikuntahakuun.")
  kotikuntaPvm: Option[LocalDate] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryLukio2019Suoritustiedot] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.lukiokoulutus
  override def raporttiName = "lukio2019suoritustietojentarkistus"

  override def auditLogParams = Map(
    "alku" -> List(alku.format(DateTimeFormatter.ISO_DATE)),
    "loppu" -> List(loppu.format(DateTimeFormatter.ISO_DATE)),
    "osasuoritustenAikarajaus" -> osasuoritustenAikarajaus.map(_.toString).toList,
    "kotikuntaPvm" -> kotikuntaPvm.map(_.format(DateTimeFormatter.ISO_DATE)).toList,
  )

  override protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiAikarajauksellaRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      alku = alku,
      loppu = loppu,
      osasuoritustenAikarajaus = osasuoritustenAikarajaus.getOrElse(false),
      kotikuntaPvm = kotikuntaPvm,
      lang = language.get,
    )
    raportitService.lukioraportti2019(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryLukio2019SuoritustiedotDocumentation {
  def xlsxExample: MassaluovutusQueryLukio2019Suoritustiedot = MassaluovutusQueryLukio2019Suoritustiedot(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    password = Some("hunter2"),
  )
}
