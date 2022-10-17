import Http from '../util/http'

const dataPath = (organisaatioOid, type, koulutustoimijaOid) =>
  `/koski/api/preferences/${organisaatioOid}/${type}${
    koulutustoimijaOid ? '?koulutustoimijaOid=' + koulutustoimijaOid : ''
  }`
const editorPath = (organisaatioOid, type, koulutustoimijaOid) =>
  `/koski/api/editor/preferences/${organisaatioOid}/${type}${
    koulutustoimijaOid ? '?koulutustoimijaOid=' + koulutustoimijaOid : ''
  }`

export const saveOrganizationalPreference = (
  organisaatioOid,
  type,
  key,
  data,
  koulutustoimijaOid
) => {
  return Http.put(
    dataPath(organisaatioOid, type, koulutustoimijaOid),
    { key, value: data },
    {
      invalidateCache: [
        dataPath(organisaatioOid, type, koulutustoimijaOid),
        editorPath(organisaatioOid, type, koulutustoimijaOid)
      ]
    }
  )
}

export const getOrganizationalPreferences = (
  organisaatioOid,
  type,
  koulutustoimijaOid
) => {
  return Http.cachedGet(editorPath(organisaatioOid, type, koulutustoimijaOid))
}

export const deleteOrganizationalPreference = (
  organisaatioOid,
  type,
  key,
  koulutustoimijaOid
) => {
  return Http.delete(
    `/koski/api/preferences/${organisaatioOid}/${type}/${key}${
      koulutustoimijaOid ? '?koulutustoimijaOid=' + koulutustoimijaOid : ''
    }`
  ).flatMap(() =>
    Http.cachedGet(editorPath(organisaatioOid, type, koulutustoimijaOid), {
      force: true
    })
  )
}
