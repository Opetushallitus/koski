import Http from '../http'

export const koodiarvoMatch = (...koodiarvot) => (value) => value && koodiarvot.includes(value.koodiarvo)
export const koodistoValues = (koodistoUri) => Http.cachedGet(`/koski/api/editor/koodit/${koodistoUri}`).map(values => values.map(t => t.data))
