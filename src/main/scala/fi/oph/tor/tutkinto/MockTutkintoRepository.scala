package fi.oph.tor.tutkinto

class MockTutkintoRepository extends TutkintoRepository {
  def tutkinnot = List(
    Tutkinto("Autoalan työnjohdon erikoisammattitutkinto", ePerusteDiaarinumero =  "1013059", tutkintoKoodi =  "357305")
  )

  override def findTutkinnot(oppilaitosId: String, query: String) = {
    tutkinnot.filter(_.toString.toLowerCase.contains(query.toLowerCase))
  }

  override def findByEPerusteDiaarinumero(id: String) = tutkinnot.filter(_.ePerusteDiaarinumero == id).headOption

  override def findPerusteRakenne(diaariNumero: String): Option[RakenneModuuli] = None
}
