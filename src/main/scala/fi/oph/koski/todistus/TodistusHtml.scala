package fi.oph.koski.todistus

import fi.oph.koski.schema._

trait TodistusHtml extends LocalizedHtml {
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
        <div class="viiva">&#160;</div>
        <div class="nimenselvennys">{myöntäjäHenkilö.nimi}</div>
        <div class="titteli">{i(myöntäjäHenkilö.titteli)}</div>
      </span>
    }
    }
  </div>
}
