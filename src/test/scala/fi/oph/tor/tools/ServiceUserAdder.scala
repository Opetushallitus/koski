package fi.oph.tor.tools

import java.time.LocalDate

import fi.oph.tor.config.TorApplication
import fi.oph.tor.henkilo.{AuthenticationServiceClient, CreateUser, UserQueryResult}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.koodisto.{KoodistoKoodi, KoodistoKoodiMetadata}
import fi.oph.tor.toruser.RemoteUserOrganisationsRepository

object ServiceUserAdder extends App {
  args match {
    case Array(username, organisaatioOid, password, lahdejarjestelma) =>
      val app: TorApplication = TorApplication()
      val authService = AuthenticationServiceClient(app.config)
      val kp = app.koodistoPalvelu

      val oid = authService.create(CreateUser.palvelu(username)) match {
        case Right(oid) =>
          println("User created")
          oid
        case Left(HttpStatus(400, _)) =>
          authService.search("testing") match {
            case r:UserQueryResult if (r.totalCount == 1) =>
              r.results(0).oidHenkilo
          }
      }

      println("Username " + username + ", oid: " + oid)

      authService.lisääOrganisaatio(oid, organisaatioOid, "oppilashallintojärjestelmä")

      authService.lisääKäyttöoikeusRyhmä(oid, organisaatioOid, RemoteUserOrganisationsRepository.käyttöoikeusryhmä)

      authService.asetaSalasana(oid, password)
      authService.syncLdap(oid)
      println("Set password " + password + ", requested LDAP sync")

      val koodiarvo = lahdejarjestelma
      val koodisto = kp.getLatestVersion("lahdejarjestelma").get

      if (!kp.getKoodistoKoodit(koodisto).toList.flatten.find(_.koodiArvo == koodiarvo).isDefined) {
        kp.createKoodi("lahdejarjestelma", KoodistoKoodi("lahdejarjestelma_" + koodiarvo, koodiarvo, List(KoodistoKoodiMetadata(Some(koodiarvo), None, Some("FI"))), 1, Some(LocalDate.now)))
        println("Luotu lähdejärjestelmäkoodi " + koodiarvo)
      }

      println("OK")
    case _ =>
      println("Usage: ServiceUserAdder <username> <organisaatio> <salasana> <lahdejärjestelmä>")
  }
}
