import Bacon from 'baconjs'

const locationBus = new Bacon.Bus()

export const navigateTo = function (path) {
  history.pushState(null, null, path)
  locationBus.push(parsePath(path))
}

window.onpopstate = function() {
  locationBus.push(parseLocation(document.location))
}

export const locationP = locationBus.toProperty(parseLocation(document.location))

export const navigateToOppija = oppija => navigateTo(`/koski/oppija/${oppija.oid}`)
export const navigateToUusiOppija = () => navigateTo('/koski/uusioppija')
export const showError = (error) => locationBus.error(error)

const parsePath = (path) => {
  let a = document.createElement('a')
  a.href = path
  return parseLocation(a)
}

function parseLocation(location) {
  return {
    path: location.pathname.replace(/(^\/?)/,"/"),
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