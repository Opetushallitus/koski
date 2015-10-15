import Polyfills from './polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import style from './style/main.less'
import handleError from './error-handler'
import {Login, userP, logout} from './Login.jsx'
import {OppijaHaku, oppijatP, searchInProgressP} from './OppijaHaku.jsx'
import {Oppija, oppijaP, uusiOppijaP, loadingOppijaP} from './Oppija.jsx'
import {routeP} from './router.js'
import {TopBar} from './TopBar.jsx'
import Http from './http'

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

const httpErrorP = stateP.changes().errors()
  .mapError(e => ({ httpStatus: e.httpStatus, isRetryable: e.httpStatus >= 500}))
  .flatMap(e => Bacon.once(e).concat(e.isRetryable
      ? Bacon.fromEvent(document.body, 'click').map({})
      : Bacon.never()
  )).toProperty({})

const errorP =  Bacon.combineWith(httpErrorP, routeP, (httpError, route) =>
  httpError.httpStatus ? httpError : route
)

const domP = stateP.combine(errorP, ({user, oppijaHaku, oppija, searchInProgress}, error) =>
    <div>
      <Error isError={error.isRetryable}/>
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

const Error = ({isError}) => {
  return isError ? <div id="error" className="error">Järjestelmässä tapahtui odottamaton virhe.<a>&#10005;</a></div> : <div id="error"></div>
}

const NotFound = () => <div className="404">404 - Etsimääsi sivua ei löytynyt</div>

domP.onValue((component) => ReactDOM.render(component, document.getElementById('content')))
domP.onError(function(e) {
  if (requiresLogin(e)) {
    logout()
  } else {
    handleError(e)
  }
})

function requiresLogin(e) {
  return e.httpStatus != 404 && e.httpStatus >= 400 && e.httpStatus < 500
}
