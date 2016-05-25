import Bacon from 'baconjs'

const b = new Bacon.Bus()

const navigate = function (path) {
  history.pushState(null, null, path)
  b.push(path)
}

window.onpopstate = function() {
  b.push(document.location.pathname)
}

export const routeP = b.toProperty(document.location.pathname)
  .map(route => {
    const match = route.match(new RegExp('/koski/oppija/(.*)'))
    const oppijaId = match ? match[1] : undefined
    if (oppijaId) {
      return {oppijaId}
    } else if (route === '/koski/uusioppija') {
      return { uusiOppija: true }
    } else if (route === '/koski/') {
      return {}
    } else {
      return { httpStatus: 404, comment: 'route not found: ' + route }
    }
  })

export const navigateToOppija = oppija => navigate(`/koski/oppija/${oppija.oid}`)
export const navigateToUusiOppija = () => navigate('/koski/uusioppija')
export const showError = (error) => b.error(error)