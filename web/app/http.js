import Bacon from 'baconjs'
import {increaseLoading, decreaseLoading} from './loadingFlag'

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
  decreaseLoading()
}

const mocks = {}
const serveMock = url => {
  let mock = mocks[url]
  delete mocks[url]
  return Bacon.once(mock.status ? mock : Bacon.Error('connection failed')).toPromise()
}
const doHttp = (url, optionsForFetch) => mocks[url] ? serveMock(url) : fetch(url, optionsForFetch)
const http = (url, optionsForFetch, options) => {
  if (options.invalidateCache){
    for (var cachedPath in http.cache) {
      if (options.invalidateCache.some(pathToInvalidate => cachedPath.startsWith(pathToInvalidate))) {
        delete http.cache[cachedPath]
      }
    }
  }
  increaseLoading()
  const promise = doHttp(url, optionsForFetch)
  promise.then(reqComplete, reqComplete)
  return Bacon.fromPromise(promise).mapError({status: 503}).flatMap(parseResponse).toProperty()
}

http.get = (url, options = {}) => http(url, { credentials: 'include' },  options)
http.post = (url, entity, options = {}) => http(url, { credentials: 'include', method: 'post', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} }, options)
http.put = (url, entity, options = {}) => http(url, { credentials: 'include', method: 'put', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} }, options)
http.mock = (url, result) => mocks[url] = result
http.cache = {}
http.cachedGet = (url, options = {}) => (http.cache[url] && !options.force)
  ? Bacon.constant(http.cache[url])
  : http.get(url, options).doAction((value) => http.cache[url] = value)
window.http = http
export default http

