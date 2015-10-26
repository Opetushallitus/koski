import './polyfills.js'
import './style/main.less'

import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, NotFound, handleError, isRetryable, errorP} from './Error.jsx'
import {Login, userP} from './Login.jsx'
import {OppijaHaku, oppijatP, searchInProgressP} from './OppijaHaku.jsx'
import {Oppija, oppijaP, uusiOppijaP, loadingOppijaP, updateResultE} from './Oppija.jsx'
import {TopBar} from './TopBar.jsx'

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

// Stays at `true` for five seconds after latest saved change. Reset to `false` when another Oppija is selected.
const savedP = updateResultE.flatMapLatest(() => Bacon.once(true).concat((stateP.changes().merge(Bacon.later(5000))).map(false))).toProperty(false).skipDuplicates()

// Renderered Virtual DOM
const domP = Bacon.combineWith(stateP, errorP(Bacon.combineAsArray(stateP, savedP)), savedP, ({user, oppijaHaku, oppija, searchInProgress}, error, saved) =>
    <div>
      <Error isError={isRetryable(error)}/>
      <TopBar user={user} saved={saved} />
      {
        error.httpStatus === 404
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