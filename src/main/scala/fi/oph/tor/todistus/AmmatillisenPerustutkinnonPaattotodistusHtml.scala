package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter

import fi.oph.tor.schema._

object AmmatillisenPerustutkinnonPaattotodistusHtml {
  def renderAmmatillisenPerustutkinnonPaattotodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, tutkintoSuoritus: AmmatillisenTutkinnonSuoritus) = {
    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-peruskoulu.css"></link>
      </head>
      <body>
        <div class="todistus ammatillinenperustutkinto">
          <h2 class="koulutustoimija">{koulutustoimija.flatMap(_.nimi).getOrElse("")}</h2>
          <h1>Päättötodistus</h1>
          <h2 class="oppilaitos">{oppilaitos.nimi.getOrElse("")}</h2>
          <div class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">{oppijaHenkilö.hetu}</span>
          </div>

          <div class="vahvistus">
            <span class="paikkakunta">Tampere<!-- TODO: paikkakuntaa ei ole datassa --></span>
            <span class="date">{tutkintoSuoritus.vahvistus.map(_.päivä).map(dateFormatter.format(_)).getOrElse("")}</span>
            {
            tutkintoSuoritus.vahvistus.flatMap(_.myöntäjäHenkilöt).toList.flatten.map { myöntäjäHenkilö =>
              <span class="allekirjoitus">
                <div class="viiva">&nbsp;</div>
                <div class="nimenselvennys">{myöntäjäHenkilö.nimi}</div>
                <div class="titteli">{myöntäjäHenkilö.titteli}</div>
              </span>
            }
            }
          </div>
        </div>
      </body>
    </html>
  }


}
