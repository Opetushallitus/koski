package fi.oph.koski.suoritusjako

case class SuoritusIdentifier(
  lähdejärjestelmänId: Option[String],
  oppilaitosOid: Option[String],
  suorituksenTyyppi: String,
  koulutusmoduulinTunniste: String
)

