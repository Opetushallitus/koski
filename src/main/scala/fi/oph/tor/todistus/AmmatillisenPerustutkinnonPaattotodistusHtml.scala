package fi.oph.tor.todistus

import java.time.format.DateTimeFormatter
import fi.oph.tor.schema._
import fi.oph.tor.tutkinto.{RakenneModuuli, SuoritustapaJaRakenne, TutkintoRakenne}

object AmmatillisenPerustutkinnonPaattotodistusHtml {
  def renderAmmatillisenPerustutkinnonPaattotodistus(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, tutkintoSuoritus: AmmatillisenTutkinnonSuoritus, rakenne: SuoritustapaJaRakenne) = {
    val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy")
    val päätasot: List[RakenneModuuli] = rakenne.rakenne.asInstanceOf[RakenneModuuli].osat.map(_.asInstanceOf[RakenneModuuli])
    val osasuoritukset = tutkintoSuoritus.osasuoritukset.toList.flatten
    def contains(rakenne: RakenneModuuli, tutkinnonOsa: AmmatillinenTutkinnonOsa) = {
      rakenne.tutkinnonOsat.map(_.tunniste).contains(tutkinnonOsa.tunniste)
    }
    def goesTo(rakenne: RakenneModuuli, tutkinnonOsa: AmmatillinenTutkinnonOsa) = {
      contains(rakenne, tutkinnonOsa) || (rakenne == päätasot.last && !päätasot.find(m => contains(m, tutkinnonOsa)).isDefined)
    }
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/tor/css/todistus-ammatillinen-perustutkinto.css"></link>
      </head>
      <body>
        <div class="todistus ammatillinenperustutkinto">
          <h2 class="koulutustoimija">{koulutustoimija.flatMap(_.nimi).getOrElse("")}</h2>
          <h2 class="oppilaitos">{oppilaitos.nimi.getOrElse("")}</h2>
          <h1>Päättötodistus</h1>
          <h2 class="koulutus">{tutkintoSuoritus.koulutusmoduuli.nimi}</h2>
          <h3 class="osaamisala-tutkintonimike">{(tutkintoSuoritus.osaamisala.toList.flatten ++ tutkintoSuoritus.tutkintonimike.toList.flatten).flatMap(_.nimi).mkString(", ")}</h3>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">({oppijaHenkilö.hetu})</span>
          </h3>
          <table class="tutkinnon-osat">
            <thead>
              <tr>
                <th class="nimi">Tutkinnon osat</th>
                <th class="laajuus">Suoritettu laajuus, osp</th>
                <th class="arvosana">Arvosana (1-3)</th>
              </tr>
            </thead>
            <tbody>
              {
                val xs = päätasot.flatMap { m =>
                  <tr class="rakennemoduuli"><td class="nimi">{m.nimi}</td></tr> ::
                  osasuoritukset.filter(osasuoritus => goesTo(m, osasuoritus.koulutusmoduuli)).map { osasuoritus =>
                    val className = "tutkinnon-osa " + osasuoritus.koulutusmoduuli.tunniste.koodiarvo
                    <tr class={className}>
                      <td class="nimi">{ osasuoritus.koulutusmoduuli.nimi }</td>
                      <td class="laajuus">{ osasuoritus.koulutusmoduuli.laajuus.map(_.arvo.toInt).getOrElse("") }</td>
                      <td class="arvosana-kirjaimin">{osasuoritus.arvosanaKirjaimin.get("fi").capitalize}</td>
                      <td class="arvosana-numeroin">{osasuoritus.arvosanaNumeroin}</td>
                    </tr>
                  }
                }
                xs
              }
              <tr class="opintojen-laajuus">
                <td class="nimi">Opiskelijan suorittamien tutkinnon osien laajuus osaamispisteinä</td>
                <td class="laajuus">{ osasuoritukset.map(_.koulutusmoduuli.laajuus.map(_.arvo.toInt).getOrElse(0)).sum }</td>
              </tr>
            </tbody>
          </table>
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
