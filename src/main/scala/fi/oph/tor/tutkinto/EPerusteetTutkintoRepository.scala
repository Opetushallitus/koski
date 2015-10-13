package fi.oph.tor.tutkinto

import org.http4s.client.blaze
import org.http4s.client.blaze.BlazeClient


class EPerusteetTutkintoRepository extends TutkintoRepository {
  private val blazeHttpClient: BlazeClient = blaze.defaultClient

  override def findTutkinnot(oppilaitosId: String, query: String) = {
    throw new UnsupportedOperationException("TODO")
  }

  override def findById(id: String) = throw new UnsupportedOperationException("TODO")
}
