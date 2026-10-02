package fi.oph.koski.perftest

import fi.oph.koski.organisaatio.{OrganisaatioHakuTulos, OrganisaatioPalveluOrganisaatio}
import fi.oph.koski.raportit.AhvenanmaanKunnat
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


  // Ahvenanmaan oppilaitoksiin ei luoda manner-Suomen opiskeluoikeuksia. Haetaan v2/hae:lla, koska
  // v2/hae/tyyppi ei palauta kotipaikkaa.
  def haeOppilaitostyypillä(tyyppi: String, tyypinNimi: String): List[OidOrganisaatio] = {
    val url: String = s"$virkailijaRoot/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&suunnitellut=true&lakkautetut=false&oppilaitostyyppi=${URLEncoder.encode(tyyppi, "UTF-8")}"
    val organisaatiOidit = EasyHttp.getJson[OrganisaatioHakuTulos](url).organisaatiot
      .filterNot(onAhvenanmaalla)
      .map(org => OidOrganisaatio(org.oid))
      .filter(org => Organisaatio.isValidOrganisaatioOid(org.oid))
    println(tyypinNimi + " määrä: " + organisaatiOidit.length)
    organisaatiOidit
  }

  private def onAhvenanmaalla(org: OrganisaatioPalveluOrganisaatio): Boolean =
    org.kotipaikkaUri.exists(uri => AhvenanmaanKunnat.onAhvenanmaalainenKunta(uri.stripPrefix("kunta_")))
}

