import Bacon from 'baconjs'
import { increaseLoading, decreaseLoading } from './loadingFlag'
import { showInternalError } from './location'
import Cookies from 'js-cookie'

const parseResponseFor = (url) => (result) => {
  if (result.status < 300) {
    if (
      result.headers
        .get('content-type')
        .toLowerCase()
        .startsWith('application/json')
    ) {
      return Bacon.fromPromise(result.json())
    }
    return Bacon.fromPromise(result.text())
  }
  if (
    result.headers &&
    result.headers
      .get('content-type')
      .toLowerCase()
      .startsWith('application/json')
  ) {
    return Bacon.fromPromise(result.json()).flatMap(
      (errorJson) =>
        new Bacon.Error({
          url,
          jsonMessage: errorJson,
          message: 'http error ' + result.status,
          httpStatus: result.status
        })
    )
  }

  return new Bacon.Error({
    url,
    message: 'http error ' + result.status,
    httpStatus: result.status
  })
}

const reqComplete = () => {
  decreaseLoading()
}

const mocks = []
const getMock = (url) => {
  for (const key in mocks) {
    if (url.startsWith(key)) {
      const mock = mocks[key]
      delete mocks[key]
      return mock
    }
  }
}
const serveMock = (mock) => {
  return Bacon.once(
    mock.status ? mock : Bacon.Error('connection failed')
  ).toPromise()
}
const doHttp = (url, optionsForFetch) => {
  const mock = getMock(url)
  return mock ? serveMock(mock) : fetch(url, optionsForFetch)
}

const http = (url, optionsForFetch, options = {}) => {
  if (options.invalidateCache) {
    for (const cachedPath in http.cache) {
      if (
        options.invalidateCache.some((pathToInvalidate) =>
          cachedPath.startsWith(pathToInvalidate)
        )
      ) {
        // console.log('clear cache', cachedPath)
        delete http.cache[cachedPath]
      }
    }
  }
  increaseLoading()
  let result = Bacon.fromPromise(doHttp(url, optionsForFetch))
    .mapError({ status: 503, url })
    .flatMap(parseResponseFor(url))
    .toProperty()
  if (options.errorMapper) {
    // errors are mapped to values or other Error events and will be handled
    result = result.flatMapError(options.errorMapper).toProperty()
  } else if (options.errorHandler) {
    // explicit error handler given
    result.onError(options.errorHandler)
  } else if (!options.willHandleErrors) {
    // unless the user promises to handle errors by { willHandleErrors: true}, we'll default to showing the internal error div
    result.onError(showInternalError)
  }
  result.onEnd(reqComplete)
  return result
}

const csrfHeader = () => ({
  CSRF: Cookies.get('CSRF'),
  'Caller-id': '1.2.246.562.10.00000000001.koski.frontend'
})

http.get = (url, options = {}, headers = {}) =>
  http(url, { credentials: 'include', headers }, options)
http.delete = (url, options = {}) =>
  http(
    url,
    { credentials: 'include', method: 'delete', headers: csrfHeader() },
    options
  )
http.post = (url, entity, options = {}) =>
  http(
    url,
    {
      credentials: 'include',
      method: 'post',
      body: JSON.stringify(entity),
      headers: { 'Content-Type': 'application/json', ...csrfHeader() }
    },
    options
  )
http.put = (url, entity, options = {}) =>
  http(
    url,
    {
      credentials: 'include',
      method: 'put',
      body: JSON.stringify(entity),
      headers: { 'Content-Type': 'application/json', ...csrfHeader() }
    },
    options
  )
http.mock = (url, result) => (mocks[url] = result)
http.cache = {}
http.cachedGet = (url, options = {}) => {
  // console.log('cachedGet', url)
  if (!http.cache[url] || options.force) {
    // console.log('not found in cache')
    http.cache[url] = http.get(url, options).doError(() => {
      // console.log('error occurred, clearing cache', url)
      delete http.cache[url]
    })
  }
  return http.cache[url]
}
window.http = http
export default http
