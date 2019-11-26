package fi.oph.koski.perftest

import java.net.URLEncoder

import fi.oph.koski.organisaatio.{OrganisaatioPalveluOrganisaatioTyyppi, OrganisaatioTyyppiHakuTulos}
import fi.oph.koski.schema.OidOrganisaatio

/**
  * Hakee eri tyyppiset oppilaitokset Opintopolun organisaatiopalvelusta.
  */
object OppilaitosImuri extends App {
  lazy val virkailijaRoot = sys.env.getOrElse("VIRKAILIJA", "https://virkailija.untuvaopintopolku.fi")

  lazy val lukiot: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_15#1")
  lazy val ammatillisetOppilaitokset: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_21#1")
  lazy val peruskoulut: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_11#1")

  println("Peruskouluja: " + peruskoulut.length)
  println("Lukioita: " + lukiot.length)
  println("Ammatillisia: " + ammatillisetOppilaitokset.length)

  def haeOppilaitostyypillä(tyyppi: String) = {
    val url: String = s"$virkailijaRoot/organisaatio-service/rest/organisaatio/v2/hae/tyyppi?aktiiviset=true&suunnitellut=true&lakkautetut=false&oppilaitostyyppi=${URLEncoder.encode(tyyppi, "UTF-8")}"
    EasyHttp.getJson[OrganisaatioTyyppiHakuTulos](url).organisaatiot.map { org: OrganisaatioPalveluOrganisaatioTyyppi => OidOrganisaatio(org.oid) }
  }
}

