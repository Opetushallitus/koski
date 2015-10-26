import Bacon from 'baconjs'

const reqE = Bacon.Bus()
const requestPending = reqE.scan(0, (a, b) => a + b).map(count => count > 0)
requestPending.onValue(pending => document.body.className = pending ? 'loading' : '')

const parseResponse = (result) => {
  if (result.status < 300) {
    if(result.headers.get('content-type').toLowerCase().startsWith('application/json')) {
      return Bacon.fromPromise(result.json())
    }
    return Bacon.fromPromise(result.text())
  }
  return new Bacon.Error({ message: 'http error ' + result.status, httpStatus: result.status })
}

const reqComplete = () => reqE.push(-1)

const mocks = {}
const serveMock = url => {
  let mock = mocks[url]
  delete mock[url]
  return Bacon.once(mock).toPromise()
}
const doHttp = (url, options) => mocks[url] ? serveMock(url) : fetch(url, options)
const http = (url, options) => {
  reqE.push(1)
  const promise = doHttp(url, options)
  promise.then(reqComplete, reqComplete)
  return Bacon.fromPromise(promise).flatMap(parseResponse)
}

http.get = (url) => http(url, { credentials: 'include' })
http.post = (url, entity) => http(url, { credentials: 'include', method: 'post', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })
http.mock = (url, result) => mocks[url] = result
window.http = http
export default http