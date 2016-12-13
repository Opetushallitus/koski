import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, navigateToUusiOppija} from './location'
import {oppijaStateP} from './Oppija.jsx'
import {modelData} from './EditorModel.js'

const oppijaHakuE = new Bacon.Bus()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(500)
  .flatMapLatest(q => (acceptableQuery(q) ? Http.get(`/koski/api/henkilo/search?query=${q}`) : Bacon.once([])).map((oppijat) => ({ results: oppijat, query: q })))

const oppijatP = Bacon.update(
  { query: '', results: [] },
  hakuTulosE, ((current, hakutulos) => hakutulos)
)

const searchInProgressP = oppijaHakuE.filter(acceptableQuery).awaiting(oppijatP.mapError().changes()).throttle(200)

export const oppijaHakuElementP = Bacon.combineWith(oppijatP, searchInProgressP, oppijaStateP, (oppijat, searchInProgress, oppija) =>
  <OppijaHaku oppijat={oppijat} valittu={modelData(oppija.valittuOppija, 'henkilö')} searching={searchInProgress}/>
)

const OppijaHakutulokset = React.createClass({
  render() {
    const {oppijat, valittu} = this.props
    const oppijatElems = oppijat.results.map((o, i) => {
        const className = valittu ? (o.oid === valittu.oid ? 'selected' : '') : ''
        return (
          <li key={i} className={className}>
            <a href={`/koski/oppija/${o.oid}`} onClick={(e) => navigateToOppija(o, e)}>{o.sukunimi}, {o.etunimet} {o.hetu}</a>
          </li>
        )}
    )

    return oppijat.results.length > 0
      ? <ul> {oppijatElems} </ul>
      : oppijat.query.length > 2
        ? <div className='no-results'>Ei hakutuloksia</div>
        : null
  }
})

export const OppijaHaku = React.createClass({
  render() {
    let {oppijat, valittu, searching} = this.props
    const className = searching ? 'oppija-haku searching' : 'oppija-haku'
    return (
      <div className={className}>
        <div>
          <h3>Hae tai lisää opiskelija</h3>
          <input id='search-query' ref='query' placeholder='henkilötunnus, nimi tai oppijanumero' onInput={(e) => oppijaHakuE.push(e.target.value)}></input>
          <a href="/koski/oppija/uusioppija" className="lisaa-oppija" onClick={navigateToUusiOppija}>Lisää opiskelija</a>
        </div>
        <div className='hakutulokset'>
          <OppijaHakutulokset oppijat={oppijat} valittu={valittu}/>
        </div>
      </div>
    )
  }
})