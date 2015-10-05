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

const errorP = stateP.changes().errors()
  .mapError(e => e).filter(e => !requiresLogin(e)).map(true)
  .merge(Bacon.fromEvent(document.body, "click").map(false))
  .toProperty(false)

const domP = stateP.combine(errorP, ({user, oppijat, valittuOppija, uusiOppija, searchInProgress}, isError) =>
  <div>
    <Error isError={isError}/>
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

const Error = ({isError}) => {
  return isError ? <div id="error" className="error">Järjestelmässä tapahtui odottamaton virhe.<a>&#10005;</a></div> : <div id="error"></div>
}

domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
domP.onError(function(e) {
  if (requiresLogin(e)) {
    logout()
  } else {
    handleError(e)
  }
})

function requiresLogin(e) {
  return e.httpStatus >= 400 && e.httpStatus < 500;
}