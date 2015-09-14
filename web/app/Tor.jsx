import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import style from "./style/main.less"
import handleError from "./error-handler"
import Http from "./http"
import {Login, userP} from "./Login.jsx"

const oppijatS = new Bacon.Bus();

const OppijaHaku = ({oppijat}) => (
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

const UserInfo = ({user}) => <div className="userInfo">{user.name}<a href="/logout">Logout</a></div>

const uiP = userP.flatMap((user) => {
  if (user) {
    const oppijatP = oppijatS.throttle(200)
      .flatMapLatest(q => Http.get(`/oppija?nimi=${q}`))
      .toProperty([])
    return oppijatP.map((oppijat) => <div><UserInfo user={user} /> <OppijaHaku oppijat={oppijat} /></div>)
  } else {
    return <Login />
  }
})

uiP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
uiP.onError(handleError)