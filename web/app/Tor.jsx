import Polyfills from "./polyfills.js"
import React from "react"
import ReactDOM from "react-dom"
import Bacon from "baconjs"
import style from "./style/main.less"
import handleError from "./error-handler"
import {Login, userP} from "./Login.jsx"
import {OppijaHakuBoksi, OppijaHakutulokset, oppijatP, oppijaP, searchInProgressP} from "./OppijaHaku.jsx"
import {Oppija} from "./Oppija.jsx"
import {TopBar} from "./TopBar.jsx"

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
        ? <div>
            <div className="oppija-haku">
              <OppijaHakuBoksi />
              <OppijaHakutulokset oppijat={oppijat} valittu={valittuOppija} searching={searchInProgress}/>
            </div>
            <Oppija oppija={valittuOppija} />
          </div>

        : <Login />
    }
  </div>
)

domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
domP.onError(handleError)