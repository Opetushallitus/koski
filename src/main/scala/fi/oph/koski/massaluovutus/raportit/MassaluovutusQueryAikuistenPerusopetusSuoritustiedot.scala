package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{AikuistenPerusopetusRaporttiRequest, OppilaitosRaporttiResponse, RaportitService}
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetusAlkuvaiheRaportti, AikuistenPerusopetusOppiaineenOppimääräRaportti, AikuistenPerusopetusPäättövaiheRaportti, AikuistenPerusopetusRaporttiType}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("Aikuisten perusopetuksen suoritustietojen tarkistus")
@Description("Palauttaa aikuisten perusopetuksen suoritustietojen tarkistusraportin.")
case class MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
  @EnumValues(Set("aikuistenPerusopetusSuoritustiedot"))
  `type`: String = "aikuistenPerusopetusSuoritustiedot",
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
  @Description("Raportin tyyppi: alkuvaihe, päättövaihe tai oppiaineenoppimäärä.")
  @EnumValues(Set("alkuvaihe", "päättövaihe", "oppiaineenoppimäärä"))
  raportinTyyppi: String,
  @Description("Jos true, osasuorituksiin rajoitetaan vain aikajakson sisällä arvioidut.")
  osasuoritustenAikarajaus: Option[Boolean] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryAikuistenPerusopetusSuoritustiedot] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.aikuistenperusopetus
  override def raporttiName = "aikuistenperusopetuksensuoritustietojentarkistus"

  override def auditLogParams = Map(
    "alku" -> List(alku.format(DateTimeFormatter.ISO_DATE)),
    "loppu" -> List(loppu.format(DateTimeFormatter.ISO_DATE)),
    "raportinTyyppi" -> List(raportinTyyppi),
    "osasuoritustenAikarajaus" -> osasuoritustenAikarajaus.map(_.toString).toList,
  )

  override protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val raporttiType: AikuistenPerusopetusRaporttiType = raportinTyyppi match {
      case "alkuvaihe" => AikuistenPerusopetusAlkuvaiheRaportti
      case "päättövaihe" => AikuistenPerusopetusPäättövaiheRaportti
      case "oppiaineenoppimäärä" => AikuistenPerusopetusOppiaineenOppimääräRaportti
      case t => throw new IllegalArgumentException(s"Tuntematon raportinTyyppi: $t")
    }

    val request = AikuistenPerusopetusRaporttiRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      alku = alku,
      loppu = loppu,
      osasuoritustenAikarajaus = osasuoritustenAikarajaus.getOrElse(false),
      raportinTyyppi = raporttiType,
      lang = language.get,
    )
    raportitService.aikuistenPerusopetus(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryAikuistenPerusopetusSuoritustiedotDocumentation {
  def xlsxExample: MassaluovutusQueryAikuistenPerusopetusSuoritustiedot = MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    raportinTyyppi = "päättövaihe",
    password = Some("hunter2"),
  )
}
