package fi.oph.tor.todistus
import java.time.format.DateTimeFormatter

import fi.oph.tor.localization.{Localizable, LocalizedString}
import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser


trait TodistusHtml {
  implicit val user: TorUser
  val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
  def lang = user.lang
  def i(s: Localizable): String = s.description.get(lang)
  def i(s: Option[Localizable]): String = s.map(i).getOrElse("")
  def decapitalize(s: String) = {
    val (head, tail) = s.splitAt(1)
    head.toLowerCase + tail
  }

  def vahvistusHTML(vahvistus: Vahvistus) = <div class="vahvistus">
    <span class="paikkakunta">{i(vahvistus.paikkakunta.nimi)}</span>
    <span class="date">{dateFormatter.format(vahvistus.päivä)}</span>
    {
    vahvistus.myöntäjäHenkilöt.map { myöntäjäHenkilö =>
      <span class="allekirjoitus">
        <div class="viiva">&nbsp;</div>
        <div class="nimenselvennys">{myöntäjäHenkilö.nimi}</div>
        <div class="titteli">{i(myöntäjäHenkilö.titteli)}</div>
      </span>
    }
    }
  </div>
}
