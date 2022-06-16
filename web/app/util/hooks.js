import { useState, useEffect } from 'react'

/**
 * Funktio, jolla Bacon.js property wrapataan Reactin useState:n avulla toteutettuun custom React hookkiin.
 * @param {*} property
 * @returns null | Property
 */
export function useBaconProperty(property) {
  const [value, setValue] = useState(null)

  useEffect(() => {
    const dispose = property.onValue(val => setValue(val))
    const errorDispose = property.onError(err => console.log(err))
    return () => {
      dispose()
      errorDispose()
    }
  })

  return value
}
