import Bacon from "baconjs"

const parseResponse = (result) => {
  if (result.status < 300) {
    return Bacon.fromPromise(result.json())
  }
  return new Bacon.Error("HTTP " + result.status)
}

export default {
  get: (url) => Bacon.fromPromise(fetch(url, { credentials: "include" })).flatMap(parseResponse),
  post: (url, entity) => Bacon.fromPromise(fetch(url, { credentials: "include", method: "post", body: JSON.stringify(entity), headers: { "Content-Type": "application/json"} })).flatMap(parseResponse)
}