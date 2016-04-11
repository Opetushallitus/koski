package fi.oph.tor.tools

import java.time.LocalDate

import fi.oph.tor.config.TorApplication
import fi.oph.tor.henkilo.{AuthenticationServiceClient, CreateUser, UserQueryResult}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.{KoodistoKoodi, KoodistoKoodiMetadata, KoodistoMuokkausPalvelu}
import fi.oph.tor.log.Logging
import fi.oph.tor.toruser.KäyttöoikeusRyhmät

object ServiceUserAdder extends App with Logging {
  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val app: TorApplication = TorApplication()
      val authService = AuthenticationServiceClient(app.config)
      val kp = app.koodistoPalvelu
      val kmp = KoodistoMuokkausPalvelu(app.config)

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

      logger.info("OK")
    case _ =>
      logger.info("Usage: ServiceUserAdder <username> <organisaatio> <salasana> <lahdejärjestelmä>")
  }
}
