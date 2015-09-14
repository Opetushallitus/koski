import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import style from "./style/main.less"
import handleError from "./error-handler"
import {Login, userP} from "./Login.jsx"
import {OppijaHaku, oppijatP} from "./OppijaHaku.jsx"
import {UserInfo} from "./UserInfo.jsx"

const uiP = userP.flatMap((user) => {
  if (user) {
    return oppijatP.map((oppijat) => <div><UserInfo user={user} /> <OppijaHaku oppijat={oppijat} /></div>)
  } else {
    return <Login />
  }
})

uiP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
uiP.onError(handleError)