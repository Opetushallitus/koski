import React from "react"
import Bacon from "baconjs"
import http from "axios"

const events = new Bacon.Bus();

const OppijaHaku = React.createClass({
    render() {
        return <div className="oppija-haku">
            <OppijaHakuBoksi />
            <OppijaHakutulokset oppijat={this.props.oppijat}/>
        </div>
    }
})

const OppijaHakuBoksi = React.createClass({
    render() {
        return <div>
            <label>Opiskelija</label>
            <input onInput={(e) => events.push(e.target.value)}></input>
        </div>
    }
})

const OppijaHakutulokset = React.createClass({
    render() {
        return <ul>{this.props.oppijat.map((oppija) => <li>{oppija.etunimet} {oppija.sukunimi} {oppija.hetu}</li>)}</ul>
    }
})

events
    .throttle(200)
    .flatMapLatest(q => Bacon.fromPromise(http.get(`/oppija?nimi=${q}`)))
    .map(".data").toProperty([]).onValue((oppijat) => React.render(
        <OppijaHaku oppijat={oppijat} />,
        document.getElementById('content')
    )
)

