package fi.oph.koski.suoritusjako

case class SuoritusIdentifier(
  lähdejärjestelmänId: Option[String],
  oppilaitosOid: String,
  suorituksenTyyppi: String,
  koulutusmoduulinTunniste: String
)

