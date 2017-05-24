import React from 'react'
import { logout } from './user'
import { routeErrorP } from './router.jsx'
import { trackRuntimeError } from './piwikTracking'
import R from 'ramda'
import Bacon from 'baconjs'
import Text from './Text.jsx'
import { texts } from './i18n'

const logError = (error) => {
  console.log('ERROR', error)
  trackRuntimeError(R.assoc('location', '' + document.location, error))
}

export const errorP = (stateP) => {
  if (window.koskiError) {
    return Bacon.constant(window.koskiError)
  } else {
    const stateErrorP = stateP.changes()
      .map(undefined)
      .mapError(error => error)
      .toProperty(undefined)
      .skipDuplicates()
      .map(error => error || {})

    return Bacon.combineWith(stateErrorP, routeErrorP, (error, routeError) =>
      error.httpStatus ? error : routeError
    )
  }
}

export const handleError = (error) => {
  if (requiresLogin(error)) {
    logout()
  } else {
    logError(error)
  }
}

export function requiresLogin(e) {
  return e.httpStatus == 401 || e.httpStatus == 403
}

const errorTexts = texts.httpStatus
const errorText = (error) => error.text || errorTexts[error.httpStatus]

export const Error = ({error}) => {
  return errorText(error) && !isTopLevel(error) ? <div id="error" className="error"><span className="error-text">{errorText(error)}</span><a>{'✕'}</a></div> : <div id="error"></div>
}

export const TopLevelError = ({error}) => (<div className="error content-area">
  <h1 className="http-status">{error.httpStatus}</h1>
  <div className="error-message">{errorText(error)} <a href="/koski"><Text name="Yritä uudestaan"/></a>{'.'}</div>
</div>)

export const isTopLevel = (error) => error.httpStatus === 404 || error.topLevel
