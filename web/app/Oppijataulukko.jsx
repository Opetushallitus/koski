import React from 'react'
import Bacon from 'baconjs'
import Pager from './Pager'
import { addQueryParams, navigateToOppija } from './location'
import { oppijaHakuElementP } from './OppijaHaku.jsx'
import PaginationLink from './PaginationLink.jsx'
import R from 'ramda'
import DatePicker from './DateRangeSelection.jsx'
import OrganisaatioPicker from './OrganisaatioPicker.jsx'
import { formatISODate, ISO2FinnishDate } from './date'
import Dropdown from './Dropdown.jsx'
import Http from './http'
import { showInternalError } from './location.js'
import SortingTableHeader from './SortingTableHeader.jsx'

export const Oppijataulukko = React.createClass({
  render() {
    let { rivit, edellisetRivit, pager, params } = this.props
    let näytettävätRivit = rivit || edellisetRivit

    return (<div className="oppijataulukko">{ näytettävätRivit ? (
      <table>
        <thead>
          <tr>
            <SortingTableHeader field='nimi' title='Nimi' defaultSort='asc'>
              <input
                placeholder="hae"
                type="text"
                defaultValue={params['nimihaku']}
                onChange={e => {
                  if (e.target.value.length >= 3 || e.target.value.length == 0) this.textFilterBus.push({'nimihaku': e.target.value})
                }}
              />
            </SortingTableHeader>
            <th className="tyyppi">
              <span className="title">Opiskeluoikeuden tyyppi</span>
              <Dropdown
                id="tyyppi-valinta"
                optionsP={this.opiskeluoikeudenTyypit}
                onSelectionChanged={option => this.filterBus.push({'opiskeluoikeudenTyyppi': option ? option.key : undefined })}
                selected={params['opiskeluoikeudenTyyppi']}
              />
            </th>
            <th className="koulutus">
              <span className="title">Koulutus</span>
              <Dropdown
                id="koulutus-valinta"
                optionsP={this.koulutus}
                onSelectionChanged={option => this.filterBus.push({'suorituksenTyyppi': option ? option.key : undefined })}
                selected={params['suorituksenTyyppi']}
              />
            </th>
            <th className="tutkinto">
              <span className="title">Tutkinto / osaamisala / nimike</span>
              <input
                placeholder="hae"
                type="text"
                defaultValue={params['tutkintohaku']}
                onChange={e => {
                  if (e.target.value.length >= 3 || e.target.value.length == 0) this.textFilterBus.push({'tutkintohaku': e.target.value})
                }}
              />
            </th>
            <th className="tila">
              <span className="title">Tila</span>
              <Dropdown
                id="tila-valinta"
                optionsP={this.opiskeluoikeudenTila}
                onSelectionChanged={option => this.filterBus.push({'opiskeluoikeudenTila': option ? option.key : undefined })}
                selected={params['opiskeluoikeudenTila']}
              />
            </th>
            <th className="oppilaitos">
              <span className="title">Oppilaitos / toimipiste</span>
              <OrganisaatioPicker
                selectedOrg={{ oid: params['toimipiste'], nimi: params['toimipisteNimi']}}
                onSelectionChanged={(org) => {this.filterBus.push(org ? { toimipiste: org.oid, toimipisteNimi: org.nimi.fi } : { toimipiste: null, toimipisteNimi: null })}}
              />
            </th>
            <SortingTableHeader field='alkamispäivä' title='Aloitus pvm'>
              <DatePicker
                selectedStartDay={params['opiskeluoikeusAlkanutAikaisintaan'] && ISO2FinnishDate(params['opiskeluoikeusAlkanutAikaisintaan'])}
                selectedEndDay={params['opiskeluoikeusAlkanutViimeistään'] && ISO2FinnishDate(params['opiskeluoikeusAlkanutViimeistään'])}
                onSelectionChanged={ range => this.filterBus.push(
                  {
                    'opiskeluoikeusAlkanutAikaisintaan': range.from ? formatISODate(range.from): undefined,
                    'opiskeluoikeusAlkanutViimeistään': range.to ? formatISODate(range.to): undefined
                  })
                }
              />
            </SortingTableHeader>
            <SortingTableHeader field='luokka' title='Luokka / ryhmä'>
              <input
                placeholder="hae"
                type="text"
                defaultValue={params['luokkahaku']}
                onChange={e => this.filterBus.push({'luokkahaku': e.target.value})}
              />
            </SortingTableHeader>
          </tr>
        </thead>
        <tbody className={rivit ? '' : 'loading'}>
          {
            näytettävätRivit.map( (opiskeluoikeus, i) => <tr className="alternating" key={i}>
              <td className="nimi"><a href={`/koski/oppija/${opiskeluoikeus.henkilö.oid}`} onClick={(e) => navigateToOppija(opiskeluoikeus.henkilö, e)}>{ opiskeluoikeus.henkilö.sukunimi + ', ' + opiskeluoikeus.henkilö.etunimet}</a></td>
              <td className="tyyppi">{ opiskeluoikeus.tyyppi.nimi.fi }</td>
              <td className="koulutus"><ul className="cell-listing">{ opiskeluoikeus.suoritukset.map((suoritus, j) => <li key={j}>{suoritus.tyyppi.nimi.fi}</li>) }</ul></td>
              <td className="tutkinto">{ opiskeluoikeus.suoritukset.map((suoritus, j) =>
                <ul className="cell-listing" key={j}>
                  {
                    <li className="koulutusmoduuli">{suoritus.koulutusmoduuli.tunniste.nimi.fi}</li>
                  }
                  {
                    (suoritus.osaamisala || []).map((osaamisala, k) => <li className="osaamisala" key={k}>{osaamisala.nimi.fi}</li>)
                  }
                  {
                    (suoritus.tutkintonimike || []).map((nimike, k) => <li className="tutkintonimike" key={k}>{nimike.nimi.fi}</li>)
                  }
                </ul>
              )}
              </td>
              <td className="tila">{ opiskeluoikeus.tila.nimi.fi }</td>
              <td className="oppilaitos"><ul className="cell-listing">{ opiskeluoikeus.suoritukset.map((suoritus, j) =>
                <li key={j} className="toimipiste">{suoritus.toimipiste.nimi.fi}</li>)
              }</ul></td>
              <td className="aloitus pvm">{ ISO2FinnishDate(opiskeluoikeus.alkamispäivä) }</td>
              <td className="luokka">{ opiskeluoikeus.luokka }</td>
            </tr>)
          }
          </tbody>
        </table>) : <div className="ajax-indicator-bg">Ladataan...</div> }
      <PaginationLink pager={pager}/>
    </div>)
  },
  componentWillMount() {
    const koodistoDropdownArvot = koodit => koodit.map(k => ({ key: k.koodiArvo, value: k.metadata.find(m => m.kieli == 'FI').nimi})).sort((a, b) => a.value.localeCompare(b.value))
    this.filterBus = Bacon.Bus()
    this.textFilterBus = Bacon.Bus()
    const opiskeluoikeudenTyyppiP = this.filterBus.filter(x => 'opiskeluoikeudenTyyppi' in x).map('.opiskeluoikeudenTyyppi').toProperty(this.props.params['opiskeluoikeudenTyyppi'])

    this.opiskeluoikeudenTyypit = Http.cachedGet('/koski/api/koodisto/opiskeluoikeudentyyppi/latest').map(koodistoDropdownArvot).doError(showInternalError)
    this.koulutus = opiskeluoikeudenTyyppiP.flatMap(ot => Http.cachedGet('/koski/api/koodisto/suoritustyypit' + (ot ? '?opiskeluoikeudentyyppi=' + ot : '')).map(koodistoDropdownArvot)).toProperty().doError(showInternalError)
    this.opiskeluoikeudenTila = Http.cachedGet('/koski/api/koodisto/koskiopiskeluoikeudentila/latest').map(koodistoDropdownArvot).doError(showInternalError)

    this.filterBus.plug(
      this.koulutus
        .filter(suoritusTyypit => this.props.params['suorituksenTyyppi'] && !R.contains(this.props.params['suorituksenTyyppi'], R.map(x => x.key, suoritusTyypit)))
        .map(() => R.objOf('suorituksenTyyppi', undefined))
    )
    this.filterBus.merge(this.textFilterBus.throttle(500)).onValue(addQueryParams)
  }
})

var edellisetRivit = null

export const oppijataulukkoContentP = (query, params) => {
  let pager = Pager('/koski/api/opiskeluoikeus/perustiedot' + query)
  let taulukkoContentP = pager.rowsP.doAction((rivit) => edellisetRivit = rivit).startWith(null).map((rivit) => <Oppijataulukko rivit={rivit} edellisetRivit={edellisetRivit} pager={pager} params={params}/>)
  return Bacon.combineWith(taulukkoContentP, oppijaHakuElementP, (taulukko, hakuElement) => ({
    content: (<div className='content-area oppijataulukko'>
      <div className="main-content">
        { hakuElement }
        <h2>Opiskelijat</h2>
      { taulukko }
      </div>
    </div>),
    title: ''
  }))
}
