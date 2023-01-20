package fi.oph.koski.perftest

import fi.oph.koski.organisaatio.OrganisaatioTyyppiHakuTulos
import fi.oph.koski.schema.{OidOrganisaatio, Organisaatio}
import fi.oph.koski.util.EnvVariables

import java.net.URLEncoder

/**
  * Hakee eri tyyppiset oppilaitokset Opintopolun organisaatiopalvelusta.
  */
object OppilaitosImuri extends App with EnvVariables {
  lazy val virkailijaRoot = env("VIRKAILIJA", "https://virkailija.untuvaopintopolku.fi")

  lazy val lukiot = haeOppilaitostyypillä("oppilaitostyyppi_15#1", "Lukio")
  lazy val ammatillisetOppilaitokset = haeOppilaitostyypillä("oppilaitostyyppi_21#1", "Ammatillinen")
  lazy val peruskoulut = haeOppilaitostyypillä("oppilaitostyyppi_11#1", "Peruskoulu")


  def haeOppilaitostyypillä(tyyppi: String, tyypinNimi: String): List[OidOrganisaatio] = {
    val url: String = s"$virkailijaRoot/organisaatio-service/rest/organisaatio/v2/hae/tyyppi?aktiiviset=true&suunnitellut=true&lakkautetut=false&oppilaitostyyppi=${URLEncoder.encode(tyyppi, "UTF-8")}"
    val organisaatiOidit = EasyHttp.getJson[OrganisaatioTyyppiHakuTulos](url).organisaatiot
      .map(org => OidOrganisaatio(org.oid))
      .filter(org => Organisaatio.isValidOrganisaatioOid(org.oid))
    println(tyypinNimi + " määrä: " + organisaatiOidit.length)
    organisaatiOidit
  }
}

