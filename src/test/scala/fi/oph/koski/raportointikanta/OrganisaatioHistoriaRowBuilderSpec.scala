package fi.oph.koski.raportointikanta

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.documentation.PerusopetusExampleData.opiskeluoikeus
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinginMedialukio, helsinki, kulosaarenAlaAste, ressunLukio, tornio}
import fi.oph.koski.raportointikanta.OrganisaatioHistoriaRowBuilder.buildOrganisaatioHistoriaRows
import fi.oph.koski.schema.OpiskeluoikeudenOrganisaatiohistoria
import org.scalatest.{FreeSpec, Matchers}

class OrganisaatioHistoriaRowBuilderSpec extends FreeSpec with Matchers {
  private val opiskeluoikeusOidilla = opiskeluoikeus(suoritukset = List()).copy(oid = Some("1.2.246.562.15.00000000001"))
  private def opiskeluoikeusOrganisaatioHistorialla(historia: List[OpiskeluoikeudenOrganisaatiohistoria]) =
    opiskeluoikeusOidilla.copy(organisaatiohistoria = Some(historia))

  "Organisaatiohistoria parsitaan raportointikannan riveiksi" - {
    "Opiskeluoikeudella ei ole organisaatiohistoriaa" in {
      val opiskeluoikeusIlmanHistoriaa = opiskeluoikeusOidilla
      val result = buildOrganisaatioHistoriaRows(opiskeluoikeusIlmanHistoriaa)
      result should equal(List(
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2008, 8, 15),
          loppu = LocalDate.of(9999, 12, 30),
          oppilaitosOid = opiskeluoikeusIlmanHistoriaa.oppilaitos.map(_.oid),
          koulutustoimijaOid = opiskeluoikeusIlmanHistoriaa.koulutustoimija.map(_.oid)
        )
      ))
    }

    "Opiskeluoikeudella on organisaatiohistoria" in {
      val opiskeluoikeusIlmanHistoriaa = opiskeluoikeusOrganisaatioHistorialla(List(
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2018, 7, 1),
          oppilaitos = Some(kulosaarenAlaAste),
          koulutustoimija = Some(helsinki)
        ),
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2019, 1, 1),
          oppilaitos = Some(ressunLukio),
          koulutustoimija = Some(tornio)
        )
      ))
      val result = buildOrganisaatioHistoriaRows(opiskeluoikeusIlmanHistoriaa)
      result should equal(List(
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2008, 8, 15),
          loppu = LocalDate.of(2018, 6, 30),
          oppilaitosOid = Some(kulosaarenAlaAste.oid),
          koulutustoimijaOid = Some(helsinki.oid)
        ),
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2018, 7, 1),
          loppu = LocalDate.of(2018, 12, 31),
          oppilaitosOid = Some(ressunLukio.oid),
          koulutustoimijaOid = Some(tornio.oid)
        ),
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2019, 1, 1),
          loppu = LocalDate.of(9999, 12, 30),
          oppilaitosOid = opiskeluoikeusIlmanHistoriaa.oppilaitos.map(_.oid),
          koulutustoimijaOid = opiskeluoikeusIlmanHistoriaa.koulutustoimija.map(_.oid)
        )
      ))
    }

    "Opiskeluoikeudella on organisaatiohistoriassa useita muutoksia samana päivänä" in {
      // Tässä on syytä selventää, että OpiskeluoikeudenOrganisaatiohistorian muutospäivä
      // on kyseisen entryn viimeistä voimassaolopäivää seuraava päivä.
      //
      // Opiskeluoikeuden kuuluminen organisaatioon siis:
      //
      // * alkaa tarkasteltavaa edeltävän OpiskeluoikeudenOrganisaatiohistoria-entryn muutospäivästä
      // * loppuu nyt tarkasteltavan OpiskeluoikeudenOrganisaatiohistoria-entryn muutospäivää edeltävään päivään
      //
      // Lisäksi ensimmäisen entryn alkupäiväksi katsotaan opiskeluoikeuden alkamispäivä.
      //
      // Tämän takia samalla muutospäivällä olevista entryistä ensimmäinen "jää voimaan".

      val opiskeluoikeusIlmanHistoriaa = opiskeluoikeusOrganisaatioHistorialla(List(
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2016, 10, 1),
          oppilaitos = Some(ressunLukio),
          koulutustoimija = Some(tornio)
        ),
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2018, 7, 1),
          oppilaitos = Some(kulosaarenAlaAste),
          koulutustoimija = Some(helsinki)
        ),
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2018, 7, 1),
          oppilaitos = Some(ressunLukio),
          koulutustoimija = Some(tornio)
        ),
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2018, 7, 1),
          oppilaitos = Some(helsinginMedialukio),
          koulutustoimija = Some(tornio)
        ),
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2019, 1, 1),
          oppilaitos = Some(ressunLukio),
          koulutustoimija = Some(helsinki)
        )
      ))
      val result = buildOrganisaatioHistoriaRows(opiskeluoikeusIlmanHistoriaa)
      result should equal(List(
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2008, 8, 15),
          loppu = LocalDate.of(2016, 9, 30),
          oppilaitosOid = Some(ressunLukio.oid),
          koulutustoimijaOid = Some(tornio.oid)
        ),
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2016, 10, 1),
          loppu = LocalDate.of(2018, 6, 30),
          oppilaitosOid = Some(kulosaarenAlaAste.oid),
          koulutustoimijaOid = Some(helsinki.oid)
        ),
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2018, 7, 1),
          loppu = LocalDate.of(2018, 12, 31),
          oppilaitosOid = Some(ressunLukio.oid),
          koulutustoimijaOid = Some(helsinki.oid)
        ),
        ROrganisaatioHistoriaRow(
          opiskeluoikeusOid = opiskeluoikeusIlmanHistoriaa.oid.get,
          alku = LocalDate.of(2019, 1, 1),
          loppu = LocalDate.of(9999, 12, 30),
          oppilaitosOid = opiskeluoikeusIlmanHistoriaa.oppilaitos.map(_.oid),
          koulutustoimijaOid = opiskeluoikeusIlmanHistoriaa.koulutustoimija.map(_.oid)
        )
      ))
    }
  }
}
