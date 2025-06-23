package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.muks.MuksRow
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class MuksRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods {

  "Muun kuin säännellyn koulutuksen raportti" - {
    val koulutustoimijaOid: String = MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.koulutustoimija
    val oppilaitosOid: String = MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos
    val expectedColumns: List[String] = List(
      "Opiskeluoikeuden oid",
      "Lähdejärjestelmä",
      "Opiskeluoikeuden tunniste lähdejärjestelmässä",
      "Koulutustoimijan nimi",
      "Oppilaitoksen nimi",
      "Toimipisteen nimi",
      "Päivitetty",
      "Yksilöity",
      "Oppijan oid",
      "hetu",
      "Sukunimi",
      "Etunimet",
      "Kotikunta",
      "Opiskeluoikeuden alkamispäivä",
      "Viimeisin opiskeluoikeuden tila",
      "Opiskeluoikeuden tilat valitun aikajakson sisällä",
      "Opintokokonaisuus",
      "Suorituksen vahvistuspäivä",
      "Rahoitukset",
      "JOTPA-asianumero",
      "Yhteislaajuus (tuntia)",
    )

    val expectedFirstRow: MuksRow = {
      val oppija = KoskiSpecificMockOppijat.jotpaMuuKuinSäänneltySuoritettu
      MuksRow(
        opiskeluoikeusOid = "",
        lähdejärjestelmä = None,
        lähdejärjestelmänId = None,
        koulutustoimijaNimi = "Jatkuva Koulutus Oy",
        oppilaitoksenNimi = "Jatkuva Koulutus Oy",
        toimipisteNimi = "Jatkuva Koulutus Oy",
        päivitetty = LocalDate.now(),
        yksilöity = true,
        oppijaOid = oppija.oid,
        hetu = oppija.hetu,
        sukunimi = oppija.sukunimi,
        etunimet = oppija.etunimet,
        kotikunta = oppija.kotikunta.getOrElse(""),
        opiskeluoikeudenAlkamispäivä = Some(LocalDate.of(2023, 1, 2)),
        viimeisinTila = Some("hyvaksytystisuoritettu"),
        opiskeluoikeudenTilatAikajaksonAikana = "lasna, hyvaksytystisuoritettu",
        opintokokonaisuus = Some("1138 Kuvallisen ilmaisun perusteet ja välineet"),
        suorituksenVahvistuspäivä = None,
        rahoitukset = "14",
        jotpaAsianumero = Some("01/5848/2023"),
        yhteislaajuus = 30.0,
      )
    }

    val expectedOppijat = Seq(
      KoskiSpecificMockOppijat.jotpaMuuKuinSäänneltySuoritettu,
      KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty,
    )

    "Oppilaitoksen raportti latautuu ja sisältää oikeat datat" in {
      val sheet = getMainDataSheet(getRaportti(oppilaitosOid))
      sheet.columnSettings.map(_._2.title) should equal(expectedColumns)
      verifyFirstRow(sheet, expectedFirstRow)
      verifyOppijat(sheet, expectedOppijat)
    }

    "Koulutustoimijan raportti latautuu ja sisältää oikeat datat" in {
      val sheet = getMainDataSheet(getRaportti(koulutustoimijaOid))
      sheet.columnSettings.map(_._2.title) should equal(expectedColumns)
      verifyFirstRow(sheet, expectedFirstRow)
      verifyOppijat(sheet, expectedOppijat)
    }

    "Aikarajaus toimii" in {
      // jotpaMuuKuinSäänneltySuoritetun opiskeluoikeus on 2.1.2023 alkaen, joten sen ei pitäisi tulla seuraavaan raporttiin mukaan
      val pvm = LocalDate.of(2023, 1, 1)
      val sheet = getMainDataSheet(getRaportti(oppilaitosOid, alku = pvm, loppu = pvm))
      verifyOppijat(sheet, Seq(KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty))
    }
  }

  private lazy val raportitService = new RaportitService(KoskiApplicationForTests)

  def getRaportti(
    organisaatioOid: String,
    alku: LocalDate = LocalDate.of(2020, 1, 1),
    loppu: LocalDate = LocalDate.of(2025, 1, 1),
    lang: String = "fi",
  ): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiRequest(organisaatioOid, None, "password", alku, loppu, lang)
    val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, lang)
    raportitService.muuKuinSäänneltyKoulutus(request, t)
  }

  def getMainDataSheet(raportti: OppilaitosRaporttiResponse): DataSheet =
    raportti.sheets.head.asInstanceOf[DataSheet]

  def verifyFirstRow(sheet: DataSheet, expected: MuksRow): Unit = {
    val row = sheet.rows.head.asInstanceOf[MuksRow]
    row should equal(expected.copy(opiskeluoikeusOid = row.opiskeluoikeusOid))
  }

  def verifyOppijat(sheet: DataSheet, oppijat: Seq[LaajatOppijaHenkilöTiedot]): Unit = {
    val actualOppijat = sheet.rows
      .map(_.asInstanceOf[MuksRow])
      .map(o => s"${o.sukunimi} ${o.etunimet} (${o.oppijaOid})")
    val expectedOppijat = oppijat
      .map(o => s"${o.sukunimi} ${o.etunimet} (${o.oid})")
    actualOppijat should contain theSameElementsAs expectedOppijat
  }
}
