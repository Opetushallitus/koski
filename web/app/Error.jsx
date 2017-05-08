import React from 'react'
import { logout } from './user'
import { routeErrorP } from './router.jsx'
import Bacon from 'baconjs'

const logError = (error) => {
  console.log('ERROR', error)
}

export const errorP = (stateP) => {
  if (window.koskiError) {
    return Bacon.constant(window.koskiError)
  } else {
    const stateErrorP = stateP.changes()
      .map(undefined)
      .mapError(error => ({ httpStatus: error.httpStatus }))
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

const errorTexts = {
  400: 'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.',
  404: 'Etsimääsi sivua ei löytynyt',
  409: 'Muutoksia ei voida tallentaa, koska toinen käyttäjä on muuttanut tietoja sivun latauksen jälkeen. Lataa sivu uudelleen.',
  500: 'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.',
  503: 'Palvelimeen ei saatu yhteyttä. Yritä myöhemmin uudelleen.'
}

export const Error = ({error}) => {
  return errorTexts[error.httpStatus] && !isTopLevel(error) ? <div id="error" className="error"><span className="error-text">{errorTexts[error.httpStatus]}</span><a>&#10005;</a></div> : <div id="error"></div>
}

export const TopLevelError = (props) => (<div className="error content-area">
  <h1 className="http-status">{props.status}</h1>
  <div className="error-message">{props.text || errorTexts[props.status]} <a href="/koski">Yritä uudestaan</a>.</div>
</div>)

export const isTopLevel = (error) => error.httpStatus === 404 || error.topLevel