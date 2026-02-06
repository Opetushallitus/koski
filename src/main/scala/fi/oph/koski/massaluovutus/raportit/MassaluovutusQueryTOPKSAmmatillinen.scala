package fi.oph.koski.massaluovutus.raportit

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.raportit.{AikajaksoRaporttiRequest, OppilaitosRaporttiResponse, RaportitService}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Title("TOPKS ammatillinen koulutus")
@Description("Palauttaa TOPKS ammatillisen koulutuksen raportin (työhön ja itsenäiseen elämään valmentava koulutus).")
@Description("Saatu tulostiedosto vastaa raporttinäkymästä ladattavaa tiedostoa, mutta se on mahdollista ladata myös paremmin koneluettavassa csv-muodossa.")
case class MassaluovutusQueryTOPKSAmmatillinen(
  @EnumValues(Set("topksAmmatillinen"))
  `type`: String = "topksAmmatillinen",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan koulutustoimijan tai oppilaitoksen oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli. CSV-muodossa vaikuttaa vain organisaatioiden nimiin.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Tutkittavan aikajakson alkamispäivä.")
  alku: LocalDate,
  @Description("Tutkittavan aikajakson päättymispäivä.")
  loppu: LocalDate,
  @Description("Salasana. Merkityksellinen vain xlsx-tiedostoille. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryTOPKSAmmatillinen] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.ammatillinenkoulutus
  override def raporttiName = "topksammatillinen"

  override def auditLogParams = Map(
    "alku" -> List(alku.format(DateTimeFormatter.ISO_DATE)),
    "loppu" -> List(loppu.format(DateTimeFormatter.ISO_DATE)),
  )

  override protected def generateReport(
    raportitService: RaportitService,
    localizationReader: LocalizationReader,
    pw: String
  )(implicit session: KoskiSpecificSession): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = organisaatioOid.get,
      downloadToken = None,
      password = pw,
      alku = alku,
      loppu = loppu,
      lang = language.get,
    )
    raportitService.topksAmmatillinen(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryTOPKSAmmatillinenDocumentation {
  def xlsxExample: MassaluovutusQueryTOPKSAmmatillinen = MassaluovutusQueryTOPKSAmmatillinen(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    language = Some("fi"),
    alku = LocalDate.of(2024, 1, 1),
    loppu = LocalDate.of(2024, 3, 31),
    password = Some("hunter2"),
  )
}
