import Http from '../http'

export const koodiarvoMatch = (koodiarvo) => (value) => value && value.koodiarvo == koodiarvo
export const koodistoValues = (koodistoUri) => Http.cachedGet(`/koski/api/editor/koodit/${koodistoUri}`).map(values => values.map(t => t.data))
