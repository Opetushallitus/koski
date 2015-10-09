import Polyfills from './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import style from './style/main.less'
import handleError from './error-handler'
import {Login, userP, logout} from './Login.jsx'
import {OppijaHaku, oppijatP, searchInProgressP} from './OppijaHaku.jsx'
import {Oppija, oppijaP, uusiOppijaP} from './Oppija.jsx'
import {opintoOikeusP} from './OpintoOikeus.jsx'
import {TopBar} from './TopBar.jsx'
import Http from './http'

const stateP = Bacon.combineTemplate({
  user: userP,
  oppijaHaku: {
    oppijat: oppijatP,
    searchInProgress: searchInProgressP
  },
  oppija: {
    valittuOppija: oppijaP,
    uusiOppija: uusiOppijaP,
  },
  opintoOikeus: opintoOikeusP
})

const domP = stateP.map(({user, oppijaHaku, oppija, opintoOikeus, searchInProgress}) =>
  <div>
    <TopBar user={user} />
    {
      user
        ? <div className='content-area'>
            <OppijaHaku oppijat={oppijaHaku.oppijat} valittu={oppija.valittuOppija} searching={oppijaHaku.searchInProgress}/>
            <Oppija oppija={oppija} opintoOikeus={opintoOikeus} />
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