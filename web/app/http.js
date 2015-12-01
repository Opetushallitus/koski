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

const reqComplete = (url) => () => {
  //console.log('complete', url)
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
  //console.log('begin', url)
  modifyReqCount(1)
  const promise = doHttp(url, options)
  promise.then(reqComplete(url), reqComplete(url))
  return Bacon.fromPromise(promise).mapError({status: 503}).flatMap(parseResponse)
}

http.get = (url) => http(url, { credentials: 'include' })
http.post = (url, entity) => http(url, { credentials: 'include', method: 'post', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })
http.put = (url, entity) => http(url, { credentials: 'include', method: 'put', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })
http.mock = (url, result) => mocks[url] = result
window.http = http
export default http