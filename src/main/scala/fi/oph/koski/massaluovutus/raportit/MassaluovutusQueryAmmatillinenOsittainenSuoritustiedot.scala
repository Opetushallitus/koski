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

@Title("Ammatillinen osittainen tutkinto suoritustietojen tarkistus")
@Description("Palauttaa ammatillisen osittaisen tutkinnon suoritustietojen tarkistusraportin.")
@Description("Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa.")
case class MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(
  @EnumValues(Set("ammatillinenOsittainenSuoritustiedot"))
  `type`: String = "ammatillinenOsittainenSuoritustiedot",
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
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus
  override def raporttiName = "ammatillinenosittainensuoritustietojentarkistus"

  override def auditLogParams = Map(
    "alku" -> List(alku.format(DateTimeFormatter.ISO_DATE)),
    "loppu" -> List(loppu.format(DateTimeFormatter.ISO_DATE)),
    "osasuoritustenAikarajaus" -> osasuoritustenAikarajaus.map(_.toString).toList,
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
      kotikuntaPvm = None,
      lang = language.get,
    )
    raportitService.ammatillinenOsittainenSuoritustietojenTarkistus(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryAmmatillinenOsittainenSuoritustiedotDocumentation {
  def xlsxExample: MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot = MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    password = Some("hunter2"),
  )
}
