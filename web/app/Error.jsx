import React from 'baret'
import Atom from 'bacon.atom'
import {logout} from './user'
import {routeErrorP} from './router.jsx'
import {trackRuntimeError} from './piwikTracking'
import R from 'ramda'
import Bacon from 'baconjs'
import Text from './Text.jsx'
import {ift} from './util'

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

const errorText = (error) => {
  if (typeof error == 'string') return error
  if (typeof error == 'array') return errorText(error[0])
  let message
  if (error.error) message = errorText(error.error)
  else if (error.text) message =  errorText(error.text)
  else if (error.jsonMessage && error.jsonMessage[0] && error.jsonMessage[0].key.startsWith('badRequest.validation')) message =  errorText(error.jsonMessage[0])
  else if (error.message) message =  errorText(error.message)
  if (message) return message
  if (error.httpStatus) return <Text name={'httpStatus.' + error.httpStatus} ignoreMissing={true}/>
}

export const Error = ({error}) => {
  let showError = errorText(error) && !isTopLevel(error)
  if (showError) { console.log('render error', error) }
  let showAtom = Atom(showError)
  return (<div id="error" className={ift(showAtom, 'error')}>
    {ift(showAtom, <span><span className="error-text">{errorText(error)}</span><a onClick={() => showAtom.set(false)}>{'✕'}</a></span>)}
  </div>)
}

export const TopLevelError = ({error}) => {
  console.log('render top-level error', error)
  return (<div className="error content-area">
    <h1 className="http-status">{error.httpStatus}</h1>
    <div className="error-message">{errorText(error)} <a href="/koski"><Text name="Yritä uudestaan"/></a>{'.'}</div>
  </div>)
}

export const isTopLevel = (error) => error.httpStatus === 404 || error.topLevel
