package fi.oph.koski.perftest

import java.net.URLEncoder

import fi.oph.koski.organisaatio.{OrganisaatioPalveluOrganisaatioTyyppi, OrganisaatioTyyppiHakuTulos}
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.util.EnvVariables

/**
  * Hakee eri tyyppiset oppilaitokset Opintopolun organisaatiopalvelusta.
  */
object OppilaitosImuri extends App with EnvVariables {
  lazy val virkailijaRoot = env("VIRKAILIJA", "https://virkailija.untuvaopintopolku.fi")

  lazy val lukiot: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_15#1", "Peruskoulu")
  lazy val ammatillisetOppilaitokset: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_21#1", "Lukio")
  lazy val peruskoulut: List[OidOrganisaatio] = haeOppilaitostyypillä("oppilaitostyyppi_11#1", "Ammatillinen")


  def haeOppilaitostyypillä(tyyppi: String, tyypinNimi: String) = {
    val url: String = s"$virkailijaRoot/organisaatio-service/rest/organisaatio/v2/hae/tyyppi?aktiiviset=true&suunnitellut=true&lakkautetut=false&oppilaitostyyppi=${URLEncoder.encode(tyyppi, "UTF-8")}"
    val organisaatiOidit = EasyHttp.getJson[OrganisaatioTyyppiHakuTulos](url).organisaatiot.map { org: OrganisaatioPalveluOrganisaatioTyyppi => OidOrganisaatio(org.oid) }
    println(tyypinNimi + " määrä: " + organisaatiOidit.length)
    organisaatiOidit
  }
}

