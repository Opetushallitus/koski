import React from 'react'
import Bacon from 'baconjs'
import Http from './http'
import {navigateToOppija, navigateToUusiOppija} from './location'

const oppijaHakuE = new Bacon.Bus()

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.debounce(500)
  .flatMapLatest(q => (acceptableQuery(q) ? Http.get(`/koski/api/henkilo/search?query=${q}`) : Bacon.once([])).map((oppijat) => ({ results: oppijat, query: q })))

const oppijatP = Bacon.update(
  { query: '', results: [] },
  hakuTulosE, ((current, hakutulos) => hakutulos)
)

const searchInProgressP = oppijaHakuE.filter(acceptableQuery).awaiting(oppijatP.mapError().changes()).throttle(200)

export const oppijaHakuElementP = Bacon.combineWith(oppijatP, searchInProgressP, (oppijat, searchInProgress) =>
  <OppijaHaku oppijat={oppijat} searching={searchInProgress}/>
)

const OppijaHakutulokset = React.createClass({
  render() {
    const {oppijat} = this.props
    const oppijatElems = oppijat.results.map((o, i) => {
        return (
          <li key={i}>
            <a href={`/koski/oppija/${o.oid}`} onClick={(e) => navigateToOppija(o, e)}>{o.sukunimi}, {o.etunimet} ({o.hetu})</a>
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
    let {oppijat, searching} = this.props
    const className = searching ? 'oppija-haku searching' : 'oppija-haku'
    return (
      <div className={className}>
        <div>
          <h3>Hae tai lisää opiskelija</h3>
          <input id='search-query' ref='query' placeholder='henkilötunnus, nimi tai oppijanumero' onInput={(e) => oppijaHakuE.push(e.target.value)}></input>
          <a href="/koski/oppija/uusioppija" className="lisaa-oppija" onClick={navigateToUusiOppija}>Lisää opiskelija</a>
        </div>
        <div className='hakutulokset'>
          <OppijaHakutulokset oppijat={oppijat}/>
        </div>
      </div>
    )
  }
})