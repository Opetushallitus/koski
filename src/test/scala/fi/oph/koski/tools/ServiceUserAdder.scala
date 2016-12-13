package fi.oph.koski.tools

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.AuthenticationServiceClient.{HenkilöQueryResult, UusiHenkilö}
import fi.oph.koski.henkilo.RemoteAuthenticationServiceClient
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät
import fi.oph.koski.log.Logging

object ServiceUserAdder extends App with Logging {
  val app: KoskiApplication = KoskiApplicationForTests
  val authService = RemoteAuthenticationServiceClient(app.config)
  val kp = app.koodistoPalvelu

  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get
      val oid: String = luoKäyttäjä(username)

      asetaOrganisaatioJaRyhmät(organisaatioOid, oid)

      authService.asetaSalasana(oid, password)
      authService.syncLdap(oid)
      logger.info("Set password " + password + ", requested LDAP sync")

      validoiLähdejärjestelmä(lahdejarjestelma)

      println(
        s"""Hei,
          |
          |Pyysitte testitunnuksia Koski-järjestelmään. Loimme käyttöönne tunnuksen:
          |
          |käyttäjätunnus: ${username}
          |salasana: ${password}
          |
          |Tunnuksella on oikeus luoda/katsella/päivittää Kosken opiskeluoikeuksia organisaatiossa ${organisaatioOid} (${organisaatio.nimi.get.get("fi")}).
          |
          |Tunnus on palvelukäyttäjätyyppinen, joten kaikkiin PUT-rajapinnan kautta lähetettyihin opiskeluoikeuksiin täytyy liittää lähdejärjestelmänId-kenttä seuraavasti:
          |
          |    "lähdejärjestelmänId": { "id": "xxxx12345", "lähdejärjestelmä": { "koodiarvo": "${lahdejarjestelma}", "koodistoUri": "lahdejarjestelma" } }
          |
          |Korvatkaa esimerkkinä käytetty id "xxxx12345" tunnisteella, jota lähdejärjestelmässänne käytetään identifioimaan kyseinen opiskeluoikeus.
          |
          |Koski-testijärjestelmän etusivu: https://extra.koski.opintopolku.fi/koski/
          |API-dokumentaatiosivu: https://extra.koski.opintopolku.fi/koski/documentation
          |
          |Tervetuloa keskustelemaan Koski-järjestelmän kehityksestä ja käytöstä yhteistyökanavallemme Flowdockiin! Lähetän sinne erilliset kutsut kohta.
          |
          |Ystävällisin terveisin,
          |___________________""".stripMargin)
    case Array(userOid, organisaatioOid) =>
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get
      asetaOrganisaatioJaRyhmät(organisaatioOid, userOid)
    case _ =>
      logger.info(
        """Usage: ServiceUserAdder <username> <organisaatio-oid> <salasana> <lahdejärjestelmä-id>
          |       ServiceUserAdder <user-oid> <organisaatio-oid>
        """.stripMargin)
  }

  def validoiLähdejärjestelmä(lahdejarjestelma: String): Unit = {
    val koodiarvo = lahdejarjestelma
    val koodisto = kp.getLatestVersion("lahdejarjestelma").get

    if (!kp.getKoodistoKoodit(koodisto).toList.flatten.find(_.koodiArvo == koodiarvo).isDefined) {
      throw new RuntimeException("Tuntematon lähdejärjestelmä " + koodiarvo)
    }
  }

  def asetaOrganisaatioJaRyhmät(organisaatioOid: String, oid: String): Unit = {
    authService.lisääOrganisaatio(oid, organisaatioOid, "oppilashallintojärjestelmä")

    val ryhmät = List(Käyttöoikeusryhmät.oppilaitosPalvelukäyttäjä)

    ryhmät.foreach { ryhmä =>
      val käyttöoikeusryhmäId = authService.käyttöoikeusryhmät.find(_.toKoskiKäyttöoikeusryhmä.map(_.nimi) == Some(ryhmä.nimi)).get.id
      authService.lisääKäyttöoikeusRyhmä(oid, organisaatioOid, käyttöoikeusryhmäId)
    }
  }

  def luoKäyttäjä(username: String): String = {
    val oid = authService.create(UusiHenkilö.palvelu(username)) match {
      case Right(oid) =>
        logger.info("User created")
        oid
      case Left(HttpStatus(400, _)) => authService.search(username) match {
        case r: HenkilöQueryResult if (r.totalCount == 1) => r.results(0).oidHenkilo
      }
    }
    logger.info("Username " + username + ", oid: " + oid)
    oid
  }
}
