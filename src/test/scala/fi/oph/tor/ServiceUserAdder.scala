package fi.oph.tor

import fi.oph.tor.config.TorApplication
import fi.oph.tor.henkilö.{AuthenticationServiceClient, CreateUser, UserQueryResult}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.user.RemoteUserRepository

object ServiceUserAdder extends App {
  args match {
    case Array(username, organisaatioOid, password) =>
      val authService = AuthenticationServiceClient(TorApplication().config)
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

      println("User oid: " + oid)

      authService.lisääOrganisaatio(oid, organisaatioOid, "oppilashallintojärjestelmä")

      authService.lisääKäyttöoikeusRyhmä(oid, organisaatioOid, RemoteUserRepository.käyttöoikeusryhmä)

      authService.asetaSalasana(oid, password)

      println("OK")
    case _ =>
      println("Usage: ServiceUserAdder <username> <organisaatio> <salasana>")
  }
}
