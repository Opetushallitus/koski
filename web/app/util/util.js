import Bacon from 'baconjs'
import * as R from 'ramda'

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

export const scrollElementBottomVisible = elem => {
  if (elem) {
    let elementTopPositionOnScreen = elem.getBoundingClientRect().y
    let elementBottomPositionOnScreen = (elementTopPositionOnScreen + elem.getBoundingClientRect().height)
    let marginBottom = window.innerHeight - elementBottomPositionOnScreen - 20
    let marginTop = elementTopPositionOnScreen - 20
    let toScroll = -marginBottom

    if (toScroll > 0 && toScroll <= marginTop) {
      window.scrollBy(0, toScroll)
    }
  }
}

export const flatMapArray = (a, f) => R.unnest(a.map(f))

// FIXME: because element.focus() option preventScroll is still experimental API, it can't really be used
// This is kind of a dirty solution to keep the page from jumping on focus, but at the time of writing at least
// Safari doesn't support focus options. Should be replaced with the use of focusOptions when properly supported by browsers
export const focusWithoutScrolling = (elem) => {
  if (!elem) return
  const {scrollX, scrollY} = window
  elem.focus()
  window.scrollTo(scrollX, scrollY)
}
