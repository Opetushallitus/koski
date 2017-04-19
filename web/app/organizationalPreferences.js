import Http from './http'

let dataPath = (organisaatioOid, type) => `/koski/api/preferences/${organisaatioOid}/${type}`
let editorPath = (organisaatioOid, type) => `/koski/api/editor/preferences/${organisaatioOid}/${type}`

export const saveOrganizationalPreference = (organisaatioOid, type, key, data) => {
  return Http.put(dataPath(organisaatioOid, type), { key, value: data}, { invalidateCache: [dataPath(organisaatioOid, type), editorPath(organisaatioOid, type)] })
}

export const getOrganizationalPreferences = (organisaatioOid, type) => {
  return Http.cachedGet(`/koski/api/editor/preferences/${organisaatioOid}/${type}`)
}