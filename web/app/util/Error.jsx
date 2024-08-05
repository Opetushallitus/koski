import React from 'baret'
import Atom from 'bacon.atom'
import { logout } from './user'
import * as R from 'ramda'
import Bacon from 'baconjs'
import Text from '../i18n/Text'
import { ift } from './util'
import { t } from '../i18n/i18n'
import { GlobalErrors } from '../components-v2/messages/GlobalErrors'

export const logError = (error) => {
  console.log('ERROR', error)
}

export const errorP = (stateP, routeErrorP) => {
  if (window.koskiError) {
    return Bacon.constant(window.koskiError)
  } else {
    const stateErrorP = stateP
      .changes()
      .map(undefined)
      .mapError((error) => error)
      .toProperty(undefined)
      .skipDuplicates()
      .map((error) => error || {})
    if (!routeErrorP) return stateErrorP
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
  return e.httpStatus === 401 || e.httpStatus === 403
}

const extractValidationErrorText = (error) => {
  if (typeof error === 'string') return error
  if (Array.isArray(error)) return extractValidationErrorText(error[0])
  if (error.error) return extractValidationErrorText(error.error)
  else if (error.message) return extractValidationErrorText(error.message)
  else return t('httpStatus.400')
}

const errorText = (error) => {
  if (error.text) return error.text
  else if (
    error.httpStatus === 400 &&
    error.jsonMessage &&
    error.jsonMessage[0] &&
    error.jsonMessage[0].key.startsWith('badRequest.validation')
  )
    return extractValidationErrorText(error.jsonMessage[0])
  else if (error.httpStatus)
    return <Text name={'httpStatus.' + error.httpStatus} ignoreMissing={true} />
}

export const Error = ({ error }) => {
  const showError = errorText(error) && !isTopLevel(error)
  if (showError) {
    console.log('render error', error)
  }
  const showAtom = Atom(showError)
  return (
    <>
      <div id="error" className={ift(showAtom, 'error')}>
        {ift(
          showAtom,
          <span>
            <span className="error-text" data-testid="error">
              {errorText(error)}
            </span>
            <a onClick={() => showAtom.set(false)}>{'✕'}</a>
          </span>
        )}
      </div>
      <GlobalErrors />
    </>
  )
}

export const TopLevelError = ({ error }) => {
  console.log('render top-level error', error)
  return (
    <div className="error content-area">
      <h1 className="http-status">{error.httpStatus}</h1>
      <div className="error-message">
        {errorText(error)}{' '}
        <a href="/koski">
          <Text name="Yritä uudestaan" />
        </a>
        {'.'}
      </div>
    </div>
  )
}

export const isTopLevel = (error) => error.httpStatus === 404 || error.topLevel
