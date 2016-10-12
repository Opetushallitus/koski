package fi.oph.koski.tools

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.RemoteAuthenticationServiceClient
import fi.oph.koski.henkilo.AuthenticationServiceClient.{UusiHenkilö, HenkilöQueryResult}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoKoodiMetadata, KoodistoMuokkausPalvelu}
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät
import fi.oph.koski.log.Logging

object ServiceUserAdder extends App with Logging {
  val app: KoskiApplication = KoskiApplicationForTests
  val authService = RemoteAuthenticationServiceClient(app.config)
  val kp = app.koodistoPalvelu
  val kmp = KoodistoMuokkausPalvelu(app.config)

  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get
      val oid: String = luoKäyttäjä(username)

      asetaOrganisaatioJaRyhmät(organisaatioOid, oid)

      authService.asetaSalasana(oid, password)
      authService.syncLdap(oid)
      logger.info("Set password " + password + ", requested LDAP sync")

      luoLähdejärjestelmä(lahdejarjestelma)

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
          |Koski-testijärjestelmän etusivu: https://koskidev.koski.oph.reaktor.fi/koski/
          |API-dokumentaatiosivu: https://koskidev.koski.oph.reaktor.fi/koski/documentation
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

  def luoLähdejärjestelmä(lahdejarjestelma: String): Unit = {
    val koodiarvo = lahdejarjestelma
    val koodisto = kp.getLatestVersion("lahdejarjestelma").get

    if (!kp.getKoodistoKoodit(koodisto).toList.flatten.find(_.koodiArvo == koodiarvo).isDefined) {
      kmp.createKoodi("lahdejarjestelma", KoodistoKoodi("lahdejarjestelma_" + koodiarvo, koodiarvo, List(KoodistoKoodiMetadata(Some(koodiarvo), None, None, Some("FI"))), 1, Some(LocalDate.now)))
      logger.info("Luotu lähdejärjestelmäkoodi " + koodiarvo)
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
