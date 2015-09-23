import Bacon from "baconjs"
import Http from "./http"

const b = new Bacon.Bus()

export const navigate = function (path) {
  history.pushState(null, null, "/tor" + path)
  b.push(path)
}

export const routeP = b.startWith(document.location.pathname)

export const oppijaP = routeP.flatMap(route => {
  var match = route.match(new RegExp("oppija/(.*)"))
  return match ? Http.get(`/tor/api/oppija?query=${match[1]}`).mapError([]).map(".0") : Bacon.once(undefined)
}).toProperty().log("oppija")
