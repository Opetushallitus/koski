package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{IBSuoritustiedotRaporttiRequest, IBTutkinnonSuoritusRaportti, OppilaitosRaporttiResponse, PreIBSuoritusRaportti, RaportitService}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("IB-tutkinnon suoritustietojen tarkistus")
@Description("Palauttaa IB-tutkinnon suoritustietojen tarkistusraportin.")
case class MassaluovutusQueryIBSuoritustiedot(
  @EnumValues(Set("ibSuoritustiedot"))
  `type`: String = "ibSuoritustiedot",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Aikajakson alku.")
  alku: LocalDate,
  @Description("Aikajakson loppu.")
  loppu: LocalDate,
  @Description("Raportin tyyppi: 'ibtutkinto' (IB-tutkinto) tai 'preiboppimaara' (Pre-IB).")
  @EnumValues(Set("ibtutkinto", "preiboppimaara"))
  raportinTyyppi: String,
  @Description("Jos true, osasuorituksiin rajoitetaan vain aikajakson sisällä arvioidut.")
  osasuoritustenAikarajaus: Option[Boolean] = None,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryIBSuoritustiedot] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ibtutkinto
  override def raporttiName = "ibsuoritustietojentarkistus"

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
    val raporttiType = raportinTyyppi match {
      case "ibtutkinto" => IBTutkinnonSuoritusRaportti
      case "preiboppimaara" => PreIBSuoritusRaportti
      case t => throw new IllegalArgumentException(s"Tuntematon raportinTyyppi: $t")
    }

    val request = IBSuoritustiedotRaporttiRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      alku = alku,
      loppu = loppu,
      osasuoritustenAikarajaus = osasuoritustenAikarajaus.getOrElse(false),
      raportinTyyppi = raporttiType,
      lang = language.get,
    )
    raportitService.ibSuoritustiedot(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryIBSuoritustiedotDocumentation {
  def xlsxExample: MassaluovutusQueryIBSuoritustiedot = MassaluovutusQueryIBSuoritustiedot(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.ressunLukio),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 12, 31),
    raportinTyyppi = "ibtutkinto",
    password = Some("hunter2"),
  )
}
