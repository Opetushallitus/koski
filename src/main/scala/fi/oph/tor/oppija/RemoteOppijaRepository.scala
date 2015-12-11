package fi.oph.tor.oppija

import fi.oph.tor.henkilö.{AuthenticationServiceClient, CreateUser, User}
import fi.oph.tor.schema.FullHenkilö

class RemoteOppijaRepository(henkilöPalveluClient: AuthenticationServiceClient) extends OppijaRepository {
  override def findOppijat(query: String): List[FullHenkilö] = {
    henkilöPalveluClient.search(query).results.map(toOppija)
  }

  override def findByOid(oid: String): Option[FullHenkilö] = henkilöPalveluClient.findByOid(oid).map(toOppija)

  override def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = henkilöPalveluClient.create(CreateUser.oppija(hetu, sukunimi, etunimet, kutsumanimi))

  private def toOppija(user: User) = FullHenkilö(user.oidHenkilo, user.hetu, user.etunimet, user.kutsumanimi, user.sukunimi)
}