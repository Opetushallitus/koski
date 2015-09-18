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

const OppijaHakuBoksi = () =>
  (
    <div>
      <label>Opiskelija
        <input id="search-query" className="stacked" onInput={(e) => oppijatE.push(e.target.value)}></input>
      </label>
      <hr></hr>
    </div>
  )

export const OppijaHakutulokset = React.createClass({
  render() {
    const {oppija} = this.state
    const {oppijat} = this.props

    const oppijatElems = oppijat.map((o, i) => {
        const className = oppija ? (o.hetu === oppija.hetu ? "selected" : "") : ""
        return <li key={i} className={className}>
          <a href="#" onClick={() => oppijaValintaE.push(o)}>{o.etunimet} {o.sukunimi} {o.hetu}</a>
        </li>
      }
    )

    return (
      <ul>
        {oppijatElems}
      </ul>
    )
  },

  getInitialState() {
    return {oppija: undefined}
  },

  componentDidMount() {
    oppijaP.onValue((o) => {this.setState({oppija: o})})
  }
})

export const OppijaHaku = ({oppijat}) => (
  <div className="oppija-haku">
    <OppijaHakuBoksi />
    <OppijaHakutulokset oppijat={oppijat}/>
  </div>
)