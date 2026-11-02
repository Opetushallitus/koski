package fi.oph.koski.massaluovutus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.massaluovutus.luokallejaaneet.{MassaluovutusQueryLuokalleJaaneetCsv, MassaluovutusQueryLuokalleJaaneetJson}
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.{MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv, MassaluovutusQueryOrganisaationOpiskeluoikeudetJson}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.time.LocalDate

// Kielitutkintorekisterin lukuoikeus on globaali mutta rajattu kielitutkintoihin. Kyselyt, jotka päästävät
// globaalin lukuoikeuden haltijan läpi ilman koulutusmuototarkistusta, eivät saa aueta sille.
class KielitutkintorekisteriGlobaaliLukuSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  private val user = MockUsers.kielitutkintorekisteriKäyttäjä
  private val organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)
  private val alku = LocalDate.of(2000, 1, 1)

  private val kyselyt: List[(String, MassaluovutusQueryParameters)] = List(
    "organisaation opiskeluoikeudet (JSON) koulutusmuodolla" ->
      MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(organisaatioOid = organisaatioOid, alkanutAikaisintaan = alku, koulutusmuoto = Some("perusopetus")),
    "organisaation opiskeluoikeudet (JSON) ilman koulutusmuotoa" ->
      MassaluovutusQueryOrganisaationOpiskeluoikeudetJson(organisaatioOid = organisaatioOid, alkanutAikaisintaan = alku),
    "organisaation opiskeluoikeudet (CSV) koulutusmuodolla" ->
      MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv(organisaatioOid = organisaatioOid, alkanutAikaisintaan = alku, koulutusmuoto = Some("perusopetus")),
    "luokalle jääneet (JSON)" -> MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = organisaatioOid),
    "luokalle jääneet (CSV)" -> MassaluovutusQueryLuokalleJaaneetCsv(organisaatioOid = organisaatioOid),
    "päällekkäiset opiskeluoikeudet" ->
      MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(format = QueryFormat.xlsx, alku = alku, loppu = LocalDate.of(2030, 1, 1), organisaatioOid = organisaatioOid),
    "perusopetuksen vuosiluokka -raportti" ->
      MassaluovutusQueryPerusopetuksenVuosiluokka(paiva = LocalDate.of(2012, 1, 1), vuosiluokka = "9", organisaatioOid = organisaatioOid),
  )

  "Kielitutkintorekisterin käyttäjä ei voi tehdä toisen organisaation kyselyä" - {
    kyselyt.foreach { case (nimi, kysely) =>
      nimi in {
        addQuery(kysely, user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }
    }
  }
}
