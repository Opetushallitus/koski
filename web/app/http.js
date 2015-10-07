import Bacon from 'baconjs'

const parseResponse = (result) => {
  if (result.status < 300) {
    if(result.headers.get('content-type').toLowerCase().startsWith('application/json')) {
      return Bacon.fromPromise(result.json())
    }
    return Bacon.fromPromise(result.text())
  }
  return new Bacon.Error({ message: 'http error ' + result.status, httpStatus: result.status })
}

export default {
  get: (url) => Bacon.fromPromise(fetch(url, { credentials: 'include' })).flatMap(parseResponse),
  post: (url, entity) => Bacon.fromPromise(fetch(url, { credentials: 'include', method: 'post', body: JSON.stringify(entity), headers: { 'Content-Type': 'application/json'} })).flatMap(parseResponse)
}