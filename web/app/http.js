import Bacon from 'baconjs'

var reqCount = 0
const modifyReqCount = delta => {
  reqCount += delta
  document.body.className = (reqCount > 0) ? 'loading' : ''
}

const parseResponse = (result) => {
  if (result.status < 300) {
    if(result.headers.get('content-type').toLowerCase().startsWith('application/json')) {
      return Bacon.fromPromise(result.json())
    }
    return Bacon.fromPromise(result.text())
  }
  return new Bacon.Error({ message: 'http error ' + result.status, httpStatus: result.status })
}

const reqComplete = () => {
  modifyReqCount(-1)
}

const mocks = {}
const serveMock = url => {
  let mock = mocks[url]
  delete mocks[url]
  return Bacon.once(mock.status ? mock : Bacon.Error('connection failed')).toPromise()
}
const doHttp = (url, options) => mocks[url] ? serveMock(url) : fetch(url, options)
const http = (url, options) => {
  modifyReqCount(1)
  const promise = doHttp(url, options)
  promise.then(reqComplete, reqComplete)
  return Bacon.fromPromise(promise).mapError({status: 503}).flatMap(parseResponse)
}

http.get = (url) => http(url, { credentials: 'include' })
http.post = (url, entity) => http(url, { credentials: 'include', method: 'post', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })
http.put = (url, entity) => http(url, { credentials: 'include', method: 'put', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })
http.mock = (url, result) => mocks[url] = result
let cache = {}
http.cachedGet = (url) => cache[url] ? Bacon.once(cache[url]) : http.get(url).doAction((value) => cache[url] = value)
window.http = http
export default http

