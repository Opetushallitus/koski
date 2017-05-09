import {navigateTo} from './location'
var currentHook = null

export const addExitHook = (msg) => {
  removeExitHook()
  currentHook = makeExitHook(msg)
  window.addEventListener('beforeunload', currentHook)
}

export const removeExitHook = () => {
  if (currentHook) {
    window.removeEventListener('beforeunload', currentHook)
    currentHook = null
  }
}

export const withExitHook = (f, useExitHook = true) => (e) => {
  e.preventDefault()
  if (useExitHook) {
    if (currentHook) {
      let result = confirm(currentHook({}))
      if (!result) {
        return
      }
    }
    removeExitHook()
  }
  f(e)
}

export const navigateWithExitHook = (href, useExitHook = true) => withExitHook(e => navigateTo(href, e), useExitHook)

let makeExitHook = (msg) => (e) => {
  e.returnValue = msg     // Gecko and Trident
  return msg              // Gecko and WebKit
}
