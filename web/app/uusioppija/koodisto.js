import Http from '../util/http'

export const koodiarvoMatch =
  (...koodiarvot) =>
  (value) =>
    value && koodiarvot.includes(value.koodiarvo)

export const koodistoValues = (koodistoUri, koodit) => {
  if (koodit !== undefined) {
    return Http.cachedGet(
      `/koski/api/editor/koodit/${koodistoUri}/${koodit.join(',')}`
    ).map((values) => values.map((t) => t.data))
  } else {
    return Http.cachedGet(`/koski/api/editor/koodit/${koodistoUri}`).map(
      (values) => values.map((t) => t.data)
    )
  }
}
