import Bacon from 'baconjs'
import R from 'ramda'
import {checkExitHook, removeExitHook} from './exitHook'
import {trackPageView} from './piwikTracking'

const locationBus = new Bacon.Bus()
let previousLocation = currentLocation()

export const navigateTo = function (path, event) {
  const prevLoc = previousLocation
  const nextLoc = parsePath(path)
  previousLocation = nextLoc
  history.pushState(null, null, path)
  if (nextLoc.path !== prevLoc.path) trackPageView(nextLoc.toString())
  locationBus.push(nextLoc)
  if (event) event.preventDefault()
}

window.onpopstate = function() {
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

let filteredLocation = currentLocation().filterQueryParams((k) => k !== 'ticket')
history.replaceState(null, null, filteredLocation.toString())
export const locationP = locationBus.toProperty(filteredLocation)

export const navigateToOppija = (oppija, event) => navigateTo(`/koski/oppija/${oppija.oid}`, event)
export const showError = (error) => locationBus.error(error)
export const showInternalError = () => locationBus.error({ httpStatus: 500 })

export const parsePath = (path) => {
  let a = document.createElement('a')
  a.href = path
  return parseLocation(a)
}

export function parseLocation(location) {
  if (typeof location == 'string') {
    return parsePath(location)
  }
  let hashStr = location.hash ? location.hash : ''
  return {
    path: location.pathname.replace(/(^\/?)/,'/'),
    params: parseQuery(location.search || ''),
    queryString: location.search || '',
    hash: location.hash,
    addQueryParams(newParams) {
      return this.replaceQueryParams(R.merge(this.params, newParams))
    },
    filterQueryParams(f) {
      let newParams = R.fromPairs(R.toPairs(this.params).filter(([key, value]) => f(key, value)))
      return this.replaceQueryParams(newParams)
    },
    replaceQueryParams(newParams) {
      let parameterPairs = R.filter(([, value]) => value != undefined && value != '', R.toPairs(newParams))
      let query = R.join('&', R.map(R.join('='), parameterPairs))
      let search = query ? '?' + query : ''
      return parseLocation({pathname: location.pathname, hash: location.hash, search: search})
    },
    toString() { return `${this.path}${hashStr}${this.queryString}` }
  }
}

export function currentLocation() {
  return parseLocation(document.location)
}

export function parseQuery(qstr) {
  if (qstr == '') return {}
  var query = {}
  var a = qstr.substr(1).split('&')
  for (var i = 0; i < a.length; i++) {
    var b = a[i].split('=')
    query[decodeURIComponent(b[0])] = decodeURIComponent(b[1] || '')
  }
  return query
}

export const appendQueryParam = (path, key, value) => path + (parsePath(path).queryString ? '&' : '?') + encodeURIComponent(key) + '=' + encodeURIComponent(value)

export const appendQueryParams = (path, params) => R.toPairs(params).reduce((l, [key, value]) => appendQueryParam(l, key, value), path)

export const navigateWithQueryParams = (newParams) => {
  navigateTo(currentLocation().addQueryParams(newParams).toString())
}
