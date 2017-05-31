package fi.oph.koski.todistus

import java.time.format.DateTimeFormatter

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizationRepository}

trait LocalizedHtml {
  implicit val user: KoskiSession
  implicit val localizationRepository: LocalizationRepository
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localizable): String = s.description.get(lang)
  def i(s: Option[Localizable]): String = s.map(i).getOrElse("")
}

object LocalizedHtml {
  def get(implicit session: KoskiSession, localizations: LocalizationRepository) = new LocalizedHtml {
    override implicit val user: KoskiSession = session
    override implicit val localizationRepository: LocalizationRepository = localizations
  }
}