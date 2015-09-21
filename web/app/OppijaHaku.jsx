import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"
import R from "ramda"

const oppijaHakuE = new Bacon.Bus();
const oppijaValintaE = new Bacon.Bus();

const acceptableQuery = (q) => q.length >= 3

export const oppijatP = oppijaHakuE.throttle(200)
  .flatMapLatest(q => (acceptableQuery(q) ? Http.get(`/tor/oppija?query=${q}`).mapError([]) : Bacon.once([])).map((oppijat) => ({ results: oppijat, query: q })))
  .toProperty({ query: "", results: [] })

export const oppijaP = Bacon.update(
  undefined,
  [oppijaValintaE], (p, n) => n,
  [oppijatP.map(".results").changes().filter((l) => l.length === 1).map(".0")], (p, n) => p ? p : n
)

export const searchInProgressP = oppijaHakuE.throttle(200).filter(acceptableQuery).awaiting(oppijatP)

export const OppijaHakuBoksi = React.createClass({
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

export const OppijaHakutulokset = ({oppijat, valittu, searching}) => {
  const oppijatElems = oppijat.results.map((o, i) => {
    const className = valittu ? (R.equals(o, valittu) ? "selected" : "") : ""
    return (
      <li key={i} className={className}>
        <a href="#" onClick={() => oppijaValintaE.push(o)}>{o.sukunimi}, {o.etunimet} {o.hetu}</a>
      </li>
    )}
  )

  const className = searching ? "searching" : ""

  return oppijat.results.length > 0
    ? <ul className={className}> {oppijatElems} </ul>
    : oppijat.query.length > 2
      ? <span className="no-results">Ei hakutuloksia</span>
      : <span></span>
}