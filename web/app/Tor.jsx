import Polyfills from "./polyfills.js"
import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import style from "./style/main.less"
import handleError from "./error-handler"
import {Login, userP, logout} from "./Login.jsx"
import {OppijaHaku, oppijatP, searchInProgressP} from "./OppijaHaku.jsx"
import {Oppija, oppijaP, uusiOppijaP} from "./Oppija.jsx"
import {TopBar} from "./TopBar.jsx"
import Http from "./http"

const stateP = Bacon.combineTemplate({
  user: userP,
  oppijat: oppijatP,
  valittuOppija: oppijaP,
  uusiOppija: uusiOppijaP,
  searchInProgress: searchInProgressP
})

const domP = stateP.map(({user, oppijat, valittuOppija, uusiOppija, searchInProgress}) =>
  <div>
    <TopBar user={user} />
    {
      user
        ? <div className="main-content">
            <OppijaHaku oppijat={oppijat} valittu={valittuOppija} searching={searchInProgress}/>
            <Oppija uusiOppija={uusiOppija} oppija={valittuOppija} />
          </div>

        : <Login />
    }
  </div>
)

domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
domP.onError(function(e) {
  if (e.httpStatus >= 400 && e.httpStatus < 500) {
    logout()
  } else {
    handleError(e)
  }
})