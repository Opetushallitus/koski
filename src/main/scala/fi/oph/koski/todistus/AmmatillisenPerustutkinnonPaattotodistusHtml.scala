package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.{RakenneModuuli, SuoritustapaJaRakenne}

class AmmatillisenPerustutkinnonPaattotodistusHtml(implicit val user: KoskiSession) extends TodistusHtml {
  def render(koulutustoimija: Option[OrganisaatioWithOid], oppilaitos: Oppilaitos, oppijaHenkilö: Henkilötiedot, tutkintoSuoritus: AmmatillisenTutkinnonSuoritus, rakenne: SuoritustapaJaRakenne) = {
    val päätasot: List[RakenneModuuli] = rakenne.rakenne match {
      case Some(moduuli: RakenneModuuli) => moduuli.osat.map(_.asInstanceOf[RakenneModuuli])
      case _ => Nil
    }
    val osasuoritukset = tutkintoSuoritus.osasuoritukset.toList.flatten
    def contains(rakenne: RakenneModuuli, tutkinnonOsa: AmmatillisenTutkinnonOsa) = {
      rakenne.tutkinnonOsat.map(_.tunniste).contains(tutkinnonOsa.tunniste)
    }
    def goesTo(rakenne: RakenneModuuli, tutkinnonOsa: AmmatillisenTutkinnonOsa) = {
      contains(rakenne, tutkinnonOsa) || (rakenne == päätasot.last && !päätasot.find(m => contains(m, tutkinnonOsa)).isDefined)
    }
    def lisätietoviite(index: Int) = "M" + (index match {
      case 0 => ""
      case n => n
    }) + ")"

    var kaikkiLisätiedot: List[AmmatillisenTutkinnonOsanLisätieto] = Nil


    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-common.css"></link>
        <link rel="stylesheet" type="text/css" href="/koski/css/todistus-ammatillinen-perustutkinto.css"></link>
      </head>
      <body>
        <div class="todistus ammatillinenperustutkinto">
          <h2 class="koulutustoimija">{i(koulutustoimija.flatMap(_.nimi))}</h2>
          <h2 class="oppilaitos">{i(oppilaitos.nimi)}</h2>
          <h1>Päättötodistus</h1>
          <h2 class="koulutus">{i(tutkintoSuoritus.koulutusmoduuli.nimi)}</h2>
          <h3 class="osaamisala-tutkintonimike">{(tutkintoSuoritus.osaamisala.toList.flatten ++ tutkintoSuoritus.tutkintonimike.toList.flatten).map(s => i(s.nimi)).mkString(", ")}</h3>
          <h3 class="oppija">
            <span class="nimi">{oppijaHenkilö.sukunimi}, {oppijaHenkilö.etunimet}</span>
            <span class="hetu">({oppijaHenkilö.hetuStr})</span>
          </h3>
          <table class="tutkinnon-osat">
            <thead>
              <tr>
                <th class="nimi">Tutkinnon osat</th>
                <th class="laajuus">Suoritettu laajuus, osp</th>
                <th colspan="2" class="arvosana">Arvosana (1-3)</th>
                <th class="lisatieto-viitteet"></th>
              </tr>
            </thead>
            <tbody>
              {
                val xs = päätasot.flatMap { m =>
                  <tr class="rakennemoduuli"><td class="nimi">{i(m.nimi)}</td></tr> ::
                  osasuoritukset.filter(osasuoritus => goesTo(m, osasuoritus.koulutusmoduuli)).map { osasuoritus =>
                    val lisätiedot = osasuoritus.lisätiedot.toList.flatten
                    val className = "tutkinnon-osa " + osasuoritus.koulutusmoduuli.tunniste.koodiarvo
                    <tr class={className}>
                      <td class="nimi">
                        { i(osasuoritus.koulutusmoduuli.nimi) }
                      </td>
                      <td class="laajuus">{ osasuoritus.koulutusmoduuli.laajuus.map(_.arvo.toInt).getOrElse("") }</td>
                      <td class="arvosana-kirjaimin">{i(osasuoritus.arvosanaKirjaimin).capitalize}</td>
                      <td class="arvosana-numeroin">
                        {i(osasuoritus.arvosanaNumeroin)}
                      </td>
                      <td class="lisatieto-viitteet">
                        {
                          lisätiedot.map { lisätieto =>
                            kaikkiLisätiedot = kaikkiLisätiedot ++ List(lisätieto)
                            <span class="lisatieto-viite">{lisätietoviite(kaikkiLisätiedot.length - 1)}</span>
                          }
                        }
                      </td>
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
          {
            val työssäoppimisenOsaamispisteet = tutkintoSuoritus.osasuoritukset.toList.flatten.flatMap(_.työssäoppimisjaksot.toList.flatten.map(_.laajuus.arvo)).sum
            if (työssäoppimisenOsaamispisteet > 0) {
              <div class="tyossa-oppiminen">
                <h4>Tutkintoon sisältyy</h4>
                <p>Työssäoppimisen kautta hankittu osaaminen ({työssäoppimisenOsaamispisteet} osp)</p>
              </div>
            }
          }
          {
            kaikkiLisätiedot.zipWithIndex.map { case(lisätieto, index) =>
              <p class="lisatieto">{lisätietoviite(index)} {i(lisätieto.kuvaus)}</p>
            }
          }
          { tutkintoSuoritus.vahvistus.toList.map(vahvistusHTML)}
        </div>
      </body>
    </html>
  }

}
