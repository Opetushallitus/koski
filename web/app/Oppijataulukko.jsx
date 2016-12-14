import React from 'react'
import Bacon from 'baconjs'
import Pager from './Pager'
import { navigateTo, navigateToOppija } from './location'
import { oppijaHakuElementP } from './OppijaHaku.jsx'
import PaginationLink from './PaginationLink.jsx'
import R from 'ramda'
import DatePicker from './DatePicker.jsx'
import { formatISODate, ISO2FinnishDate } from './date'
import Dropdown from './Dropdown.jsx'
import Http from './http'

export const Oppijataulukko = React.createClass({
  render() {
    let { rivit, edellisetRivit, pager, params } = this.props
    let [ sortBy, sortOrder ] = params.sort ? params.sort.split(':') : ['nimi', 'asc']
    let näytettävätRivit = rivit || edellisetRivit

    return (<div className="oppijataulukko">{ näytettävätRivit ? (
      <table>
        <thead>
          <tr>
            <th className={sortBy == 'nimi' ? 'nimi sorted' : 'nimi'}>
              <Sorter field='nimi' sortBus={this.sortBus} sortBy={sortBy} sortOrder={sortOrder}>Nimi</Sorter>
              <input
                placeholder="hae"
                type="text"
                defaultValue={params['nimihaku']}
                onChange={e => {
                  if (e.target.value.length >= 3 || e.target.value.length == 0) this.textFilterBus.push({'nimihaku': e.target.value})
                }}
              />
            </th>
            <th className="tyyppi">
              <span className="title">Opiskeluoikeuden tyyppi</span>
              <Dropdown
                optionsP={this.opiskeluoikeudenTyypit}
                onSelectionChanged={option => this.filterBus.push({'opiskeluoikeudenTyyppi': option ? option.key : undefined })}
                selected={params['opiskeluoikeudenTyyppi']}
              />
            </th>
            <th className="koulutus">
              <span className="title">Koulutus</span>
              <Dropdown
                optionsP={this.koulutus}
                onSelectionChanged={option => this.filterBus.push({'suorituksenTyyppi': option ? option.key : undefined })}
                selected={params['suorituksenTyyppi']}
              />
            </th>
            <th className="tutkinto"><span className="title">Tutkinto / osaamisala / nimike</span></th>
            <th className="tila">
              <span className="title">Tila</span>
              <Dropdown
                optionsP={this.opiskeluoikeudenTila}
                onSelectionChanged={option => this.filterBus.push({'opiskeluoikeudenTila': option ? option.key : undefined })}
                selected={params['opiskeluoikeudenTila']}
              />
            </th>
            <th className="oppilaitos"><span className="title">Oppilaitos</span></th>
            <th className={sortBy == 'alkamispäivä' ? 'aloitus sorted': 'aloitus'}>
              <Sorter field='alkamispäivä' sortBus={this.sortBus} sortBy={sortBy} sortOrder={sortOrder}>Aloitus pvm</Sorter>
              <DatePicker
                selectedDay={params['opiskeluoikeusAlkanutAikaisintaan'] && ISO2FinnishDate(params['opiskeluoikeusAlkanutAikaisintaan'])}
                onSelectionChanged={date => this.filterBus.push({'opiskeluoikeusAlkanutAikaisintaan': date ? formatISODate(date) : undefined })}
              />
            </th>
            <th className={sortBy == 'luokka' ? 'luokka sorted': 'luokka'}>
              <Sorter field='luokka' sortBus={this.sortBus} sortBy={sortBy} sortOrder={sortOrder}>Luokka / ryhmä</Sorter>
              <input
                placeholder="hae"
                type="text"
                defaultValue={params['luokkahaku']}
                onChange={e => this.filterBus.push({'luokkahaku': e.target.value})}
              />
            </th>
          </tr>
        </thead>
        <tbody className={rivit ? '' : 'loading'}>
          {
            näytettävätRivit.map( (opiskeluoikeus, i) => <tr key={i}>
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
  componentWillMount() {
    const koodistoDropdownArvot = koodit => koodit.map(k => ({ key: k.koodiArvo, value: k.metadata.find(m => m.kieli == 'FI').nimi})).sort((a, b) => a.value.localeCompare(b.value))
    this.sortBus = Bacon.Bus()
    this.filterBus = Bacon.Bus()
    this.textFilterBus = Bacon.Bus()
    const opiskeluoikeudenTyyppiP = this.filterBus.filter(x => 'opiskeluoikeudenTyyppi' in x).map('.opiskeluoikeudenTyyppi').toProperty(this.props.params['opiskeluoikeudenTyyppi'])

    this.opiskeluoikeudenTyypit = Http.get('/koski/api/koodisto/opiskeluoikeudentyyppi/latest').map(koodistoDropdownArvot)
    this.koulutus = opiskeluoikeudenTyyppiP.flatMap(ot => Http.get('/koski/api/koodisto/suoritustyypit' + (ot ? '?opiskeluoikeudentyyppi=' + ot : '')).map(koodistoDropdownArvot)).toProperty()
    this.opiskeluoikeudenTila = Http.get('/koski/api/koodisto/koskiopiskeluoikeudentila/latest').map(koodistoDropdownArvot)

    this.filterBus.plug(
      this.koulutus
        .filter(suoritusTyypit => this.props.params['suorituksenTyyppi'] && !R.contains(this.props.params['suorituksenTyyppi'], R.map(x => x.key, suoritusTyypit)))
        .map(() => R.objOf('suorituksenTyyppi', undefined))
    )
    this.filterBus.plug(this.textFilterBus.throttle(500))
  },
  componentDidMount() {
    const toParameterPairs = params => R.filter(([, value]) => !!value, R.toPairs(R.merge(this.props.params, params)))

    this.sortBus.merge(this.filterBus)
      .map(param => R.join('&', R.map(R.join('='), toParameterPairs(param))))
      .onValue(query => navigateTo(`/koski/?${query}`))
  }
})

const Sorter = React.createClass({
  render() {
    let { field, sortBus, sortBy, sortOrder } = this.props
    let selected = sortBy == field

    return (<div className="sorting" onClick={() => sortBus.push({ sort: field + ':' + (selected ? (sortOrder == 'asc' ? 'desc' : 'asc') : 'asc')})}>
      <div className="title">{this.props.children}</div>
      <div className="sort-indicator">
        <div className={selected && sortOrder == 'asc' ? 'asc selected' : 'asc'}></div>
        <div className={selected && sortOrder == 'desc' ? 'desc selected' : 'desc'}></div>
      </div>
    </div>)
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
