import Bacon from "baconjs"
import Http from "./http"

const b = new Bacon.Bus()

export const navigate = function (path) {
  history.pushState(null, null, "/tor" + path)
  b.push(path)
}

export const routeP = b.startWith(document.location.pathname)
