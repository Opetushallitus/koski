package fi.oph.koski.editor

import java.time.format.DateTimeFormatter

import fi.oph.common.schema.Localized
import fi.oph.common.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository

trait LocalizedHtml {
  implicit val user: KoskiSession
  implicit val localizationRepository: LocalizationRepository
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localized): String = s.description.get(lang)
  def i(s: Option[Localized]): String = s.map(i).getOrElse("")
}

object LocalizedHtml {
  def get(implicit session: KoskiSession, localizations: LocalizationRepository) = new LocalizedHtml {
    override implicit val user: KoskiSession = session
    override implicit val localizationRepository: LocalizationRepository = localizations
  }
}
