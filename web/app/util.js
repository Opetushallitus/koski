import Bacon from 'baconjs'
import R from 'ramda'

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

export const ift = (obs, value) => toObservable(obs).map(show => show ? value : null)


export const splitName = (name) => {
  let n = name.trim().split().join('')
  return R.filter(R.identity, R.uniq(n.split(/\s/g).concat(n.split(/[\s\-]/g))))
}


export const capitalizeName = (name) => {
  let n0 = name.toLowerCase()
  let n1 = n0.split(/\s/g).map(s => s.length > 0 ? s[0].toUpperCase() + s.slice(1) : '').join(' ')
  let n2 = n1.split(/-/g ).map(s => s.length > 0 ? s[0].toUpperCase() + s.slice(1) : '').join('-')
  return n2
}
