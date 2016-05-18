package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter

import fi.oph.tor.localization.Localizable
import fi.oph.tor.toruser.TorUser

trait LocalizedHtml {
  implicit val user: TorUser
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localizable): String = s.description.get(lang)
  def i(s: Option[Localizable]): String = s.map(i).getOrElse("")
}
