import React from "react"
import Bacon from "baconjs"
import http from "axios"
import style from "./style/main.less"

const oppijatS = new Bacon.Bus();

const OppijaHaku = React.createClass({
    render() {
        return (
            <div className="oppija-haku">
                <OppijaHakuBoksi />
                <OppijaHakutulokset oppijat={this.props.oppijat}/>
            </div>
        )
    }
})

const OppijaHakuBoksi = React.createClass({
    render() {
        return (
            <div>
                <label>Opiskelija</label>
                <input onInput={(e) => oppijatS.push(e.target.value)}></input>
            </div>
        )
    }
})

const OppijaHakutulokset = React.createClass({
    render() {
        const oppijat = this.props.oppijat.map((oppija, i) => <li key={i}>{oppija.etunimet} {oppija.sukunimi} {oppija.hetu}</li>)
        return (
            <ul>
                {oppijat}
            </ul>
        )
    }
})

const oppijatP = oppijatS.throttle(200)
    .flatMapLatest(q => Bacon.fromPromise(http.get(`/oppija?nimi=${q}`))).map(".data")
    .toProperty([])

oppijatP.onValue((oppijat) => React.render(<OppijaHaku oppijat={oppijat} />, document.getElementById('content')))

