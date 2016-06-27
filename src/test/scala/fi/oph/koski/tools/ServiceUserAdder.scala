package fi.oph.koski.tools

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{AuthenticationServiceClient, CreateUser, UserQueryResult}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoKoodi, KoodistoKoodiMetadata, KoodistoMuokkausPalvelu}
import fi.oph.koski.log.Logging
import fi.oph.koski.koskiuser.KäyttöoikeusRyhmät

object ServiceUserAdder extends App with Logging {
  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val app: KoskiApplication = KoskiApplication()
      val authService = AuthenticationServiceClient(app.config)
      val kp = app.koodistoPalvelu
      val kmp = KoodistoMuokkausPalvelu(app.config)
      val organisaatio = app.organisaatioRepository.getOrganisaatio(organisaatioOid).get

      val oid = authService.create(CreateUser.palvelu(username)) match {
        case Right(oid) =>
          logger.info("User created")
          oid
        case Left(HttpStatus(400, _)) =>
          authService.search(username) match {
            case r:UserQueryResult if (r.totalCount == 1) =>
              r.results(0).oidHenkilo
          }
      }

      logger.info("Username " + username + ", oid: " + oid)

      authService.lisääOrganisaatio(oid, organisaatioOid, "oppilashallintojärjestelmä")

      authService.lisääKäyttöoikeusRyhmä(oid, organisaatioOid, KäyttöoikeusRyhmät(app.config).readWrite)

      authService.asetaSalasana(oid, password)
      authService.syncLdap(oid)
      logger.info("Set password " + password + ", requested LDAP sync")

      val koodiarvo = lahdejarjestelma
      val koodisto = kp.getLatestVersion("lahdejarjestelma").get

      if (!kp.getKoodistoKoodit(koodisto).toList.flatten.find(_.koodiArvo == koodiarvo).isDefined) {
        kmp.createKoodi("lahdejarjestelma", KoodistoKoodi("lahdejarjestelma_" + koodiarvo, koodiarvo, List(KoodistoKoodiMetadata(Some(koodiarvo), None, None, Some("FI"))), 1, Some(LocalDate.now)))
        logger.info("Luotu lähdejärjestelmäkoodi " + koodiarvo)
      }

      println(
        s"""Hei,
          |
          |Pyysitte testitunnuksia Koski-järjestelmään. Loin käyttöönne tunnuksen:
          |
          |käyttäjätunnus: ${username}
          |salasana: ${password}
          |
          |Tunnuksella on oikeus luoda/katsella/päivittää Kosken opiskeluoikeuksia organisaatiossa ${organisaatioOid} (${organisaatio.nimi.get.get("fi")}).
          |
          |Koski-testijärjestelmän etusivu: https://koskidev.koski.oph.reaktor.fi/koski/
          |API-dokumentaatiosivu: https://koskidev.koski.oph.reaktor.fi/koski/documentation
          |
          |Tervetuloa keskustelemaan Koski-järjestelmän kehityksestä ja käytöstä yhteistyökanavallemme Flowdockiin! Lähetän sinne erilliset kutsut kohta.
          |
          |Ystävällisin terveisin,
          |___________________""".stripMargin)
    case _ =>
      logger.info("Usage: ServiceUserAdder <username> <organisaatio> <salasana> <lahdejärjestelmä>")
  }
}
