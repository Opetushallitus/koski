import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import Http from "./http"

const oppijatE = new Bacon.Bus();
const oppijaValintaE = new Bacon.Bus();

export const oppijatP = oppijatE.throttle(200)
  .flatMapLatest(q => q.length >= 3 ? Http.get(`/tor/oppija?query=${q}`) : Bacon.once([]))
  .toProperty([])

export const oppijaP = Bacon.update(
  undefined,
  [oppijaValintaE], (p, n) => n,
  [oppijatP.changes().filter((l) => l.length === 1).map(".0")], (p, n) => p ? p : n
)

export const OppijaHakuBoksi = React.createClass({
  render() {
    return (
      <div>
        <label>Opiskelija
          <input id="search-query" className="stacked" ref="query" onInput={(e) => oppijatE.push(e.target.value)}></input>
        </label>
        <hr></hr>
      </div>
    )
  },

  componentDidMount() {
    this.refs.query.focus()
  }
})

export const OppijaHakutulokset = ({oppijat, valittu}) => {
  const oppijatElems = oppijat.map((o, i) => {
    const className = valittu ? (o.hetu === valittu.hetu ? "selected" : "") : ""
    return (
      <li key={i} className={className}>
        <a href="#" onClick={() => oppijaValintaE.push(o)}>{o.sukunimi}, {o.etunimet} {o.hetu}</a>
      </li>
    )}
  )

  return (
    <ul>
      {oppijatElems}
    </ul>
  )
}