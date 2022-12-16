package fi.oph.koski.suoritusjako

case class SuoritusIdentifier(
  lähdejärjestelmänId: Option[String],
  opiskeluoikeusOid: Option[String], // 15.12.2022 kenttä lisätty jälkeenpäin, tieto puuttuu kaikista jaoista ajalta ennen muutosta
  oppilaitosOid: Option[String],
  suorituksenTyyppi: String,
  koulutusmoduulinTunniste: String
)

