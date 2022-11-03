import http from './http'
import Bacon from 'baconjs'

export const editorPrototypeP = (modelName) => {
  const url = `/koski/api/editor/prototype/fi.oph.koski.schema.${encodeURIComponent(
    modelName
  )}`
  return http
    .cachedGet(url, {
      errorMapper: (e) => {
        switch (e.errorStatus) {
          case 404:
            return null
          case 500:
            return null
          default:
            return new Bacon.Error(e)
        }
      }
    })
    .toProperty()
}

export const alternativesP = (alternativePath) =>
  http
    .cachedGet(alternativePath, {
      errorMapper: (e) => {
        switch (e.errorStatus) {
          case 404:
            return null
          case 500:
            return null
          default:
            return new Bacon.Error(e)
        }
      }
    })
    .toProperty()
