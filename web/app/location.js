import Bacon from 'baconjs'
import R from 'ramda'

const locationBus = new Bacon.Bus()

export const previousLocation = () => sessionStorage.previousLocation && parsePath(sessionStorage.previousLocation)

export const navigateTo = function (path, event) {
  sessionStorage.previousLocation = currentLocation().toString()
  history.pushState(null, null, path)
  locationBus.push(parsePath(path))
  if (event) event.preventDefault()
}

window.onpopstate = function() {
  locationBus.push(currentLocation())
}

export const locationP = locationBus.toProperty(currentLocation())

export const navigateToOppija = (oppija, event) => navigateTo(`/koski/oppija/${oppija.oid}`, event)
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
      let parameterPairs = R.filter(([, value]) => !!value, R.toPairs(newParams))
      let query = R.join('&', R.map(R.join('='), parameterPairs))
      let search = query ? '?' + query : ''
      return parseLocation({pathname: location.pathname, search: search})
    },
    toString() { return `${this.path}${this.queryString}` }
  }
}

export function currentLocation() {
  return parseLocation(document.location)
}

function parseQuery(qstr) {
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