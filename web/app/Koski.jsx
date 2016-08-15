import './polyfills.js'
import './style/main.less'

import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import {Error, TopLevelError, isTopLevel, handleError, errorP} from './Error.jsx'
import {Login, userP} from './Login.jsx'
import {OppijaHaku, oppijatP, searchInProgressP} from './OppijaHaku.jsx'
import {Oppija, oppijaStateP, selectOppijaE, updateResultE} from './Oppija.jsx'
import {modelData} from './EditorModel.js'
import {TopBar} from './TopBar.jsx'

// Application state to be rendered
const stateP = Bacon.combineTemplate({
  user: userP,
  oppijaHaku: {
    oppijat: oppijatP,
    searchInProgress: searchInProgressP
  },
  oppija: oppijaStateP
})

// Stays at `true` for five seconds after latest saved change. Reset to `false` when another Oppija is selected.
const savedP = updateResultE.flatMapLatest(() => Bacon.once(true).concat((selectOppijaE.merge(Bacon.later(5000))).map(false))).toProperty(false).skipDuplicates()

// Renderered Virtual DOM
const domP = Bacon.combineWith(stateP, errorP(Bacon.combineAsArray(stateP, savedP)), savedP, ({user, oppijaHaku, oppija, searchInProgress}, error, saved) =>
    <div>
      <Error error={error}/>
      <TopBar user={user} saved={saved} />
      {
        isTopLevel(error)
          ? <TopLevelError status={error.httpStatus} text={error.text} />
          : ( user
              ? <div className='content-area'>
                <OppijaHaku oppijat={oppijaHaku.oppijat} valittu={modelData(oppija.valittuOppija, 'henkilö')} searching={oppijaHaku.searchInProgress}/>
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