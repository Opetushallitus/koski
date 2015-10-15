import Bacon from 'baconjs'
import Http from './http'

const b = new Bacon.Bus()

const navigate = function (path) {
  history.pushState(null, null, '/tor' + path)
  b.push(path)
}

export const routeP = b.toProperty(document.location.pathname)
  .map(route => {
    const match = route.match(new RegExp('/tor/oppija/(.*)'))
    const oppijaId = match ? match[1] : undefined
    if (oppijaId) {
      return {oppijaId}
    } else if (route === '/tor/uusioppija') {
      return { uusiOppija: true }
    } else if (route === '/tor/') {
      return {}
    } else {
      return { httpStatus: 404, text: 'route not found: ' + route }
    }
  })

export const navigateToOppija = oppija => navigate(`/tor/oppija/${oppija.oid}`)
export const navigateToUusiOppija = () => navigate('/tor/uusioppija')
export const showError = (error) => b.error(error)