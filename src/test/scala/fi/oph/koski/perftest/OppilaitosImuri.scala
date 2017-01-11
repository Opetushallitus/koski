package fi.oph.koski.perftest

import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.organisaatio.{OrganisaatioHakuTulos, OrganisaatioPalveluOrganisaatio}
import fi.oph.koski.schema.OidOrganisaatio

/**
  * Hakee eri tyyppiset oppilaitokset Opintopolun organisaatiopalvelusta.
  */
object OppilaitosImuri extends App {
  lazy val http = Http(sys.env.getOrElse("VIRKAILIJA", "https://dev.koski.opintopolku.fi"))

  lazy val lukiot: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_15#1")
  lazy val ammatillisetOppilaitokset: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_21#1")
  lazy val peruskoulut: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_11#1")

  println("Peruskouluja: " + peruskoulut.length)
  println("Lukioita: " + lukiot.length)
  println("Ammatillisia: " + ammatillisetOppilaitokset.length)

  def haeOppilaitostyypillä(tyyppi: String) = http.get(uri"/organisaatio-service/rest/organisaatio/v2/hae/tyyppi?aktiiviset=true&suunnitellut=true&lakkautetut=false&oppilaitostyyppi=$tyyppi")(Http.parseJson[OrganisaatioHakuTulos]).run
    .organisaatiot.map {org: OrganisaatioPalveluOrganisaatio => OidOrganisaatio(org.oid)}
}

