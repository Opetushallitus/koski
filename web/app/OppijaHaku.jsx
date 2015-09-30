import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
import R from "ramda"
import {navigate, navigateToOppija, navigateToUusiOppija, routeP} from "./router.js"
import {oppijaP} from "./Oppija.jsx"

const oppijaHakuE = new Bacon.Bus();

const acceptableQuery = (q) => q.length >= 3

const hakuTulosE = oppijaHakuE.throttle(200)
  .flatMapLatest(q => (acceptableQuery(q) ? Http.get(`/tor/api/oppija?query=${q}`) : Bacon.once([])).map((oppijat) => ({ results: oppijat, query: q })))

const oppijaE = oppijaP.toEventStream().filter(Bacon._.id)

export const oppijatP = Bacon.update(
  { query: "", results: [] },
  hakuTulosE, ((current, hakutulos) => hakutulos),
  oppijaE, ((current, valittu) => current.results.filter((oppija) => oppija.oid === valittu.oid).length ? current : { query: "", results: [valittu] })
)

oppijaP.sampledBy(oppijatP.map(".results").changes(), (oppija, oppijat) => ({ oppija: oppija, oppijat: oppijat }))
  .filter(({oppija, oppijat}) => !oppija && oppijat.length == 1)
  .map(".oppijat.0")
  .onValue(navigateToOppija)

export const searchInProgressP = oppijaHakuE.filter(acceptableQuery).awaiting(oppijatP).throttle(200)


const OppijaHakuBoksi = React.createClass({
  render() {
    return (
      <div>
        <label>Opiskelija
          <input id="search-query" className="stacked" ref="query" onInput={(e) => oppijaHakuE.push(e.target.value)}></input>
        </label>
        <hr></hr>
      </div>
    )
  },

  componentDidMount() {
    this.refs.query.focus()
  }
})

const OppijaHakutulokset = React.createClass({
  render() {
    const {oppijat, valittu} = this.props
    const oppijatElems = oppijat.results.map((o, i) => {
        const className = valittu ? (R.equals(o, valittu) ? "selected" : "") : ""
        return (
          <li key={i} className={className}>
            <a onClick={this.selectOppija.bind(this, o)}>{o.sukunimi}, {o.etunimet} {o.hetu}</a>
          </li>
        )}
    )

    return oppijat.results.length > 0
      ? <ul> {oppijatElems} </ul>
      : oppijat.query.length > 2
      ? <div className="no-results">Ei hakutuloksia<a className="lisaa-oppija" onClick={navigateToUusiOppija}>Lisää oppija</a></div>
      : <span></span>
  },

  selectOppija(oppija) {
    navigateToOppija(oppija)
  }
})

export const OppijaHaku = ({oppijat, valittu, searching}) => {
  const className = searching ? "oppija-haku searching" : "oppija-haku"

  return <div className={className}>
    <OppijaHakuBoksi />
    <div className="hakutulokset">
      <OppijaHakutulokset oppijat={oppijat} valittu={valittu}/>
    </div>
  </div>
}