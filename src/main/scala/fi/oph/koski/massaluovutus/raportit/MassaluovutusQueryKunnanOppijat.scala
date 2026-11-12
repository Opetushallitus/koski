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

@Title("Kunnan oppijat muilla opetuksen järjestäjillä")
@Description("Palauttaa oppilaskohtaisen listan kunnan perusopetusikäisistä asukkaista, joiden opetuksen järjestäjä on valittuna päivänä muu kuin kunta itse. Kysely vaatii kunnassa oikeudet sekä perusopetuksen että esiopetuksen opiskeluoikeuksiin ja luottamuksellisiin tietoihin.")
case class MassaluovutusQueryKunnanOppijat(
  @EnumValues(Set("kunnanOppijat"))
  `type`: String = "kunnanOppijat",
  @EnumValues(Set(QueryFormat.xlsx))
  format: String = QueryFormat.xlsx,
  @Description("Kyselyyn otettavan kunnan oid. Jos ei ole annettu, päätellään käyttäjän käyttöoikeuksista.")
  organisaatioOid: Option[Organisaatio.Oid] = None,
  @Description("Palautettavien tuloksien kieli.")
  @EnumValues(Set("fi", "sv", "en"))
  language: Option[String] = None,
  @Description("Päivämäärä, jolta raportti lasketaan. Kuntalaiset ratkaistaan kotikunnan perusteella samalta päivältä.")
  paiva: LocalDate,
  @Description("Salasana xlsx-tiedostolle. Jos ei annettu, salasana generoidaan automaattisesti. Salasana palautetaan tulosten yhteydessä.")
  password: Option[String] = None,
) extends MassaluovutusRaporttiBase[MassaluovutusQueryKunnanOppijat] {

  override def opiskeluoikeudenTyyppi = OpiskeluoikeudenTyyppi.perusopetus
  override def raporttiName = "kunnanoppijat"

  // Raportilla on kuntalaisten tietoja muidenkin järjestäjien opiskeluoikeuksista
  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean =
    super.queryAllowed(application) && withKoskiSpecificSession { u =>
      val accessResolver = RaportitAccessResolver(application)
      organisaatioOid.exists(oid =>
        accessResolver.hasKaikkiOpiskeluoikeudenTyypit(oid, RaportitAccessResolver.kotikuntalaskelmanOpiskeluoikeudenTyypit)(u) &&
          accessResolver.hasLuottamuksellisetTiedot(oid)(u) &&
          accessResolver.kuntakoodi(oid).isDefined
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
    raportitService.kunnanOppijat(request, localizationReader)
  }

  override protected def withFilledParams(orgOid: Organisaatio.Oid, lang: String) =
    copy(organisaatioOid = Some(orgOid), language = Some(lang))
}

object QueryKunnanOppijatDocumentation {
  def xlsxExample: MassaluovutusQueryKunnanOppijat = MassaluovutusQueryKunnanOppijat(
    format = QueryFormat.xlsx,
    organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    language = Some("fi"),
    paiva = LocalDate.of(2026, 12, 31),
    password = Some("hunter2"),
  )
}
