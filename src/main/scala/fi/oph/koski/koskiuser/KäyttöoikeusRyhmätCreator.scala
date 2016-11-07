package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.henkilo.AuthenticationServiceClient.UusiKäyttöoikeusryhmä
import fi.oph.koski.henkilo.RemoteAuthenticationServiceClient
import fi.oph.koski.koodisto.KoodistoPalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus

object KäyttöoikeusRyhmätCreator extends Logging {
  def luoKäyttöoikeusRyhmät(config: Config): Unit = {
    val client = RemoteAuthenticationServiceClient(config)
    val olemassaOlevatRyhmät = client.käyttöoikeusryhmät
    val koodistopalvelu: KoodistoPalvelu = KoodistoPalvelu.withoutCache(config)
    val oppilaitostyypit: List[String] = koodistopalvelu.getLatestVersion("oppilaitostyyppi").flatMap(koodistopalvelu.getKoodistoKoodit(_)).toList.flatten.map(_.koodiArvo)

    Käyttöoikeusryhmät.käyttöoikeusryhmät.filter(_.nimi.startsWith("koski-")) foreach { ryhmä =>
      val olemassaOlevaRyhmä = olemassaOlevatRyhmät.find(olemassaOlevaRyhmä => olemassaOlevaRyhmä.toKoskiKäyttöoikeusryhmä.map(_.nimi) == Some(ryhmä.nimi))

      val (organisaatioTyypit, palveluroolit) = ryhmä match {
        case r: OrganisaationKäyttöoikeusryhmä => (oppilaitostyypit, r.palveluroolit)
        case r: GlobaaliKäyttöoikeusryhmä => (List(Opetushallitus.organisaatioOid), r.palveluroolit)
      }

      val tiedot = UusiKäyttöoikeusryhmä(ryhmä.nimi, ryhmä.nimi, ryhmä.nimi, organisaatioTyypit = organisaatioTyypit, palvelutRoolit = palveluroolit)

      olemassaOlevaRyhmä match {
        case Some(o) =>
          logger.info("päivitetään " + ryhmä)
          client.muokkaaKäyttöoikeusryhmä(o.id, tiedot)
        case None =>
          logger.info("luodaan " + ryhmä)
          client.luoKäyttöoikeusryhmä(tiedot)
      }
    }
  }
}
