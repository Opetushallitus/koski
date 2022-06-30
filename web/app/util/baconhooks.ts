import { useState, useEffect } from 'react'
import type { Property, Observable } from 'baconjs'

/**
 * Funktio, jolla Bacon.js property wrapataan Reactin useState:n avulla toteutettuun custom React hookkiin.
 * @param {Property} property Bacon.js property
 * @returns null | Property
 */
export function useBaconProperty<V>(property: Property<V>) {
  const [value, setValue] = useState<V | null>(null)

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


/**
 * Funktio, jolla Bacon.js Observable wrapataan Reactin useState:n avulla toteutettuun custom React hookkiin.
 * @param {Observable} observable Bacon.JS observable
 * @returns null | Property
 */
export function useBaconObservable<V>(observable: Observable<V>) {
  const [value, setValue] = useState<V | null>(null)

  useEffect(() => {
    const dispose = observable.onValue(val => setValue(val))
    const errorDispose = observable.onError(err => console.log(err))
    return () => {
      dispose()
      errorDispose()
    }
  })

  return value
}
