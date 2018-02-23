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
