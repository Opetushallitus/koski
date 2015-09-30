import Bacon from "baconjs"
import Http from "./http"

const b = new Bacon.Bus()

const navigate = function (path) {
  history.pushState(null, null, "/tor" + path)
  b.push(path)
}

export const routeP = b.toProperty(document.location.pathname)

export const navigateToOppija = oppija => navigate(`/oppija/${oppija.oid}`)
export const navigateToUusiOppija = () => navigate("/uusioppija")