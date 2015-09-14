import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import http from "axios"
import style from "./style/main.less"

import {Login, userP} from "./Login.jsx"

const oppijatS = new Bacon.Bus();

const OppijaHaku = ({oppijat}) =>
    (
        <div className="oppija-haku">
            <OppijaHakuBoksi />
            <OppijaHakutulokset oppijat={oppijat}/>
        </div>
    )

const OppijaHakuBoksi = () =>
    (
        <div>
            <label>Opiskelija</label>
            <input onInput={(e) => oppijatS.push(e.target.value)}></input>
        </div>
    )

const OppijaHakutulokset = ({oppijat}) => {
        const oppijatElems = oppijat.map((oppija, i) => <li key={i}>{oppija.etunimet} {oppija.sukunimi} {oppija.hetu}</li>)
        return (
            <ul>
                {oppijatElems}
            </ul>
        )
    }

userP.flatMap((u) => {
    if (u) {
        const oppijatP = oppijatS.throttle(200)
            .flatMapLatest(q => Bacon.fromPromise(http.get(`/oppija?nimi=${q}`))).map(".data")
            .toProperty([])
        return oppijatP.map((oppijat) => <OppijaHaku oppijat={oppijat} />)
    } else {
        return <Login />
    }
}).onValue((component) => ReactDOM.render(component, document.getElementById('content')))





