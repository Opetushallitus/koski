package fi.oph.koski.editor

import java.time.format.DateTimeFormatter

import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema.Localized

trait LocalizedHtml {
  implicit val user: KoskiSpecificSession
  implicit val localizationRepository: LocalizationRepository
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localized): String = s.description.get(lang)
  def i(s: Option[Localized]): String = s.map(i).getOrElse("")
}

object LocalizedHtml {
  def get(implicit session: KoskiSpecificSession, localizations: LocalizationRepository) = new LocalizedHtml {
    override implicit val user: KoskiSpecificSession = session
    override implicit val localizationRepository: LocalizationRepository = localizations
  }
}
