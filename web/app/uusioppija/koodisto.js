import Bacon from 'baconjs'
import Http from '../util/http'
import { useState, useEffect } from 'react'

export const koodiarvoMatch =
  (...koodiarvot) =>
  (value) =>
    value && koodiarvot.includes(value.koodiarvo)

export const koodistoValues = (koodistoUri) =>
  Http.cachedGet(`/koski/api/editor/koodit/${koodistoUri}`).map((values) =>
    values.map((t) => t.data)
  )

export const useKoodistovalues = (...koodistoUris) => {
  const [values, setValues] = useState([])
  useEffect(() => {
    const property = Bacon.combineWith(
      (...koodistoProperties) =>
        koodistoProperties.reduce((prev, curr) => [...prev, ...curr], []),
      koodistoUris.map((koodistoUri) => koodistoValues(koodistoUri))
    )
    const dispose = property.onValue((val) => setValues(val))
    // eslint-disable-next-line no-console
    const errorDispose = property.onError((err) => console.error(err))
    return () => {
      dispose()
      errorDispose()
    }
  }, [])
  return values
}
