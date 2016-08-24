import Bacon from 'baconjs'

const b = new Bacon.Bus()

export const navigateTo = function (path) {
  history.pushState(null, null, path)
  b.push(path)
}

window.onpopstate = function() {
  b.push(document.location.pathname)
}

export const locationP = b.toProperty(document.location.pathname)

export const navigateToOppija = oppija => navigateTo(`/koski/oppija/${oppija.oid}`)
export const navigateToUusiOppija = () => navigateTo('/koski/uusioppija')
export const showError = (error) => b.error(error)