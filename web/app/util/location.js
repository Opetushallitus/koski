import Bacon from 'baconjs'
import * as R from 'ramda'
import { checkExitHook, removeExitHook } from './exitHook'
import { trackPageView } from '../tracking/piwikTracking'

const locationBus = new Bacon.Bus()
let previousLocation = currentLocation()

export const navigateTo = function (path, event) {
  if (
    event &&
    (event.altKey || event.shiftKey || event.metaKey || event.ctrlKey)
  )
    return
  const prevLoc = previousLocation
  const nextLoc = parsePath(path)
  previousLocation = nextLoc
  history.pushState(null, null, path)
  if (nextLoc.path !== prevLoc.path) trackPageView(nextLoc.toString())
  locationBus.push(nextLoc)
  if (event) event.preventDefault()
}

export const replaceLocation = (path) => {
  const prevLoc = previousLocation
  const nextLoc = parsePath(path)
  previousLocation = nextLoc
  history.replaceState(null, null, path)
  if (nextLoc.path !== prevLoc.path) trackPageView(nextLoc.toString())
}

window.onpopstate = function () {
  if (!checkExitHook()) {
    // Back-button navigation cancelled by exit hook
    history.pushState(null, null, previousLocation.toString())
    return
  }
  const prevLoc = previousLocation
  const nextLoc = currentLocation()
  previousLocation = nextLoc
  if (nextLoc.path !== prevLoc.path) trackPageView(nextLoc.toString())
  locationBus.push(nextLoc)
}

locationBus.mapError().onValue(removeExitHook)

const filteredLocation = currentLocation().filterQueryParams(
  (k) => k !== 'ticket'
)
history.replaceState(null, null, filteredLocation.toString())
export const locationP = locationBus.toProperty(filteredLocation)

export const navigateToOppija = (oppija, event) =>
  navigateTo(`/koski/oppija/${oppija.oid}`, event)
export const showError = (error) => locationBus.error(error)
export const showInternalError = () => locationBus.error({ httpStatus: 500 })

export const parsePath = (path) => {
  const a = document.createElement('a')
  a.href = path
  if (a.pathname === '') {
    // IE does not populate all fields when setting relative href. Href returns an absolute
    // URL after set though, so this hack can be used to populate all fields on IE.
    // eslint-disable-next-line no-self-assign
    a.href = a.href
  }
  return parseLocation(a)
}

export function parseLocation(location) {
  if (typeof location === 'string') {
    return parsePath(location)
  }
  const hashStr = location.hash ? location.hash : ''
  return {
    path: location.pathname.replace(/\/$/, '').replace(/(^\/?)/, '/'),
    params: parseQuery(location.search || ''),
    queryString: location.search || '',
    hash: location.hash,
    addQueryParams(newParams) {
      return this.replaceQueryParams(R.mergeRight(this.params, newParams))
    },
    filterQueryParams(f) {
      const newParams = R.fromPairs(
        R.toPairs(this.params).filter(([key, value]) => f(key, value))
      )
      return this.replaceQueryParams(newParams)
    },
    replaceQueryParams(newParams) {
      const parameterPairs = R.filter(
        ([, value]) => value !== undefined && value !== '' && value !== false,
        encodeParams(newParams)
      )
      const query = R.join('&', R.map(R.join('='), parameterPairs))
      const search = query ? '?' + query : ''
      return parseLocation({
        pathname: location.pathname,
        hash: location.hash,
        search
      })
    },
    toString() {
      return `${this.path}${hashStr}${this.queryString}`
    }
  }
}

export function currentLocation() {
  return parseLocation(document.location)
}

export function parseQuery(qstr) {
  if (qstr == '') return {}
  const query = {}
  const a = qstr.substr(1).split('&')
  for (let i = 0; i < a.length; i++) {
    const b = a[i].split('=')
    query[decodeURIComponent(b[0])] = decodeURIComponent(b[1] || '')
  }
  return query
}

export const appendQueryParams = (path, params) =>
  parsePath(path).addQueryParams(params).toString()

export const navigateWithQueryParams = (newParams) => {
  navigateTo(currentLocation().addQueryParams(newParams).toString())
}

function encodeParam(param) {
  return typeof param === 'string' ? encodeURIComponent(param) : param
}

function encodeParams(params) {
  return R.toPairs(params).map(([key, value]) => [
    encodeURIComponent(key),
    encodeParam(value)
  ])
}
