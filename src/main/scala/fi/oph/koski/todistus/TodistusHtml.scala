package fi.oph.koski.todistus

import java.text.NumberFormat

import fi.oph.koski.localization.Locale._
import fi.oph.koski.schema._

trait TodistusHtml extends LocalizedHtml {
  def laajuus(suoritus: Suoritus): Float = suoritus.koulutusmoduuli.laajuus.map(_.arvo)
    .getOrElse(suoritus.osasuoritusLista.map(laajuus).sum)
  val decimalFormat = NumberFormat.getInstance(finnish)

  def decapitalize(s: String) = {
    val (head, tail) = s.splitAt(1)
    head.toLowerCase + tail
  }

  def vahvistusHTML(vahvistus: Vahvistus) = <div class="vahvistus">
    {
      vahvistus.getPaikkakunta.toList.map { p =>
        <span class="paikkakunta">{i(p.nimi)}</span>
      }
    }
    <span class="date">{dateFormatter.format(vahvistus.päivä)}</span>
    {
    vahvistus.myöntäjäHenkilöt.map { myöntäjäHenkilö =>
      <span class="allekirjoitus">
        <div class="viiva">&#160;</div>
        <div class="nimenselvennys">{myöntäjäHenkilö.nimi}</div>
        <div class="titteli">{i(myöntäjäHenkilö.getTitteli)}</div>
      </span>
    }
    }
  </div>
}
