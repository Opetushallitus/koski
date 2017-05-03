import Bacon from 'baconjs'

export const doActionWhileMounted = (stream, action) => stream.doAction(action).map(null).toProperty().startWith(null)

export const parseBool = (b, defaultValue) => {
  if (typeof b === 'string') {
    return b === 'true'
  }
  if (b === undefined) {
    b = defaultValue
  }
  return b
}

const isObs = x => x instanceof Bacon.Observable

export const toObservable = (x) => isObs(x) ? x : Bacon.constant(x)