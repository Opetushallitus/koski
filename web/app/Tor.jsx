import Polyfills from './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import style from './style/main.less'
import {Error, NotFound, handleError, isRetryable, errorP} from './Error.jsx'
import {Login, userP, logout} from './Login.jsx'
import {OppijaHaku, oppijatP, searchInProgressP} from './OppijaHaku.jsx'
import {Oppija, oppijaP, uusiOppijaP, loadingOppijaP} from './Oppija.jsx'
import {routeP} from './router.js'
import {TopBar} from './TopBar.jsx'
import Http from './http'

// Application state to be rendered
const stateP = Bacon.combineTemplate({
  user: userP,
  oppijaHaku: {
    oppijat: oppijatP,
    searchInProgress: searchInProgressP
  },
  oppija: {
    loading: loadingOppijaP,
    valittuOppija: oppijaP,
    uusiOppija: uusiOppijaP
  }
})

// Renderered Virtual DOM
const domP = stateP.combine(errorP(stateP), ({user, oppijaHaku, oppija, searchInProgress}, error) =>
    <div>
      <Error isError={isRetryable(error)}/>
      <TopBar user={user}/>
      {
        error.httpStatus == 404
          ? <NotFound/>
          : ( user
              ? <div className='content-area'>
                <OppijaHaku oppijat={oppijaHaku.oppijat} valittu={oppija.valittuOppija} searching={oppijaHaku.searchInProgress}/>
                <Oppija oppija={oppija}/>
              </div>
              : <Login />
           )
      }
    </div>
)

// Render to DOM
domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))

// Handle errors
domP.onError(handleError)