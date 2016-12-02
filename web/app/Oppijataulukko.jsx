import React from 'react'
import Bacon from 'baconjs'
import Pager from './Pager'
import { navigateTo, navigateToOppija } from './location'
import { ISO2FinnishDate } from './date'
import { oppijaHakuElementP } from './OppijaHaku.jsx'
import { elementWithLoadingIndicator } from './AjaxLoadingIndicator.jsx'
import PaginationLink from './PaginationLink.jsx'

export const Oppijataulukko = React.createClass({
  render() {
    let { rivit, pager, params: {sort: sorting} } = this.props
    let [ sortBy, sortOrder ] = sorting ? sorting.split(':') : ['nimi', 'asc']

    let sort = field => ({ sortBy: field, sortOrder: sortBy == field ? (sortOrder == 'asc' ? 'desc' : 'asc') : 'asc' })

    return (<div className="oppijataulukko">{ rivit ? (
      <table>
        <thead>
          <tr>
            <th className="nimi" onClick={() => this.sortBus.push(sort('nimi'))}>Nimi</th>
            <th className="tyyppi">Opiskeluoikeuden tyyppi</th>
            <th className="koulutus">Koulutus</th>
            <th className="tutkinto">Tutkinto / osaamisala / nimike</th>
            <th className="tila">Tila</th>
            <th className="oppilaitos">Oppilaitos</th>
            <th className="aloitus" onClick={() => this.sortBus.push(sort('alkamispäivä'))}>Aloitus pvm</th>
            <th className="luokka" onClick={() => this.sortBus.push(sort('luokka'))}>Luokka / ryhmä</th>
          </tr>
        </thead>
        <tbody>
          {
            rivit.map( (opiskeluoikeus, i) => <tr key={i}>
              <td className="nimi"><a href={`/koski/oppija/${opiskeluoikeus.henkilö.oid}`} onClick={(e) => navigateToOppija(opiskeluoikeus.henkilö, e)}>{ opiskeluoikeus.henkilö.sukunimi + ', ' + opiskeluoikeus.henkilö.etunimet}</a></td>
              <td className="tyyppi">{ opiskeluoikeus.tyyppi.nimi.fi }</td>
              <td className="koulutus">{ opiskeluoikeus.suoritukset.map((suoritus, j) => <span key={j}>{suoritus.tyyppi.nimi.fi}</span>) } </td>
              <td className="tutkinto">{ opiskeluoikeus.suoritukset.map((suoritus, j) =>
                <span key={j}>
                  {
                    <span className="koulutusmoduuli">{suoritus.koulutusmoduuli.tunniste.nimi.fi}</span>
                  }
                  {
                    (suoritus.osaamisala || []).map((osaamisala, k) => <span className="osaamisala" key={k}>{osaamisala.nimi.fi}</span>)
                  }
                  {
                    (suoritus.tutkintonimike || []).map((nimike, k) => <span className="tutkintonimike" key={k}>{nimike.nimi.fi}</span>)
                  }
                </span>
              )}
              </td>
              <td className="tila">{ opiskeluoikeus.tila.nimi.fi }</td>
              <td className="oppilaitos">{ opiskeluoikeus.oppilaitos.nimi.fi }</td>
              <td className="aloitus pvm">{ ISO2FinnishDate(opiskeluoikeus.alkamispäivä) }</td>
              <td className="luokka">{ opiskeluoikeus.luokka }</td>
            </tr>)
          }
          </tbody>
        </table>) : <div className="ajax-indicator-bg">Ladataan...</div> }
      <PaginationLink pager={pager}/>
    </div>)
  },
  componentDidMount() {
    this.sortBus = Bacon.Bus()
    this.sortBus.map(sort => `sort=${sort.sortBy}:${sort.sortOrder}`).onValue(query => navigateTo(`/koski/?${query}`))
  }
})


export const oppijataulukkoContentP = (query, params) => {
  let pager = Pager('/koski/api/opiskeluoikeus/perustiedot' + query)
  let taulukkoContentP = elementWithLoadingIndicator(pager.rowsP.map((rivit) => <Oppijataulukko rivit={rivit} pager={pager} params={params}/>))
  return Bacon.combineWith(taulukkoContentP, oppijaHakuElementP, (taulukko, hakuElement) => ({
    content: (<div className='content-area'>
      { hakuElement }
      <div className="main-content">
      { taulukko }
      </div>
    </div>),
    title: ''
  }))
}