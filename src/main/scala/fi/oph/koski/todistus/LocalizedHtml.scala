package fi.oph.koski.todistus

import java.time.format.DateTimeFormatter

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.Localizable

trait LocalizedHtml {
  implicit val user: KoskiUser
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localizable): String = s.description.get(lang)
  def i(s: Option[Localizable]): String = s.map(i).getOrElse("")
}
