import Bacon from 'baconjs'
import R from 'ramda'

const locationBus = new Bacon.Bus()

export const navigateTo = function (path, event) {
  history.pushState(null, null, path)
  locationBus.push(parsePath(path))
  if (event) event.preventDefault()
}

window.onpopstate = function() {
  locationBus.push(parseLocation(document.location))
}

export const locationP = locationBus.toProperty(parseLocation(document.location))

export const navigateToOppija = (oppija, event) => navigateTo(`/koski/oppija/${oppija.oid}`, event)
export const navigateToUusiOppija = (event) => navigateTo('/koski/uusioppija', event)
export const showError = (error) => locationBus.error(error)
export const showInternalError = () => locationBus.error({ httpStatus: 500 })

export const parsePath = (path) => {
  let a = document.createElement('a')
  a.href = path
  return parseLocation(a)
}

export function parseLocation(location) {
  return {
    path: location.pathname.replace(/(^\/?)/,'/'),
    params: parseQuery(location.search),
    queryString: location.search || ''
  }
}

function parseQuery(qstr) {
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

export const addQueryParams = (param) => {
  var currentLocation = parseLocation(document.location)
  let toParameterPairs = params => R.filter(([, value]) => !!value, R.toPairs(R.merge(currentLocation.params, params)))
  let query = R.join('&', R.map(R.join('='), toParameterPairs(param)))
  navigateTo(`${currentLocation.path}?${query}`)
}