import Polyfills from "./polyfills.js"
import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import style from "./style/main.less"
import handleError from "./error-handler"
import {Login, userP} from "./Login.jsx"
import {OppijaHaku, oppijaP, oppijatP, searchInProgressP} from "./OppijaHaku.jsx"
import {Oppija} from "./Oppija.jsx"
import {TopBar} from "./TopBar.jsx"
import Http from "./http"

const stateP = Bacon.combineTemplate({
  user: userP,
  oppijat: oppijatP,
  valittuOppija: oppijaP,
  searchInProgress: searchInProgressP
})

const domP = stateP.map(({user, oppijat, valittuOppija, searchInProgress}) =>
  <div>
    <TopBar user={user} />
    {
      user
        ? <div className="main-content">
            <OppijaHaku oppijat={oppijat} valittu={valittuOppija} searching={searchInProgress}/>
            <Oppija oppijat={oppijat} oppija={valittuOppija} />
          </div>

        : <Login />
    }
  </div>
)

domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
domP.onError(handleError)