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

export const navigateWithExitHook = (href, useExitHook = true) => (e) => {
  if (useExitHook) {
    e.preventDefault()
    if (currentHook) {
      let result = confirm(currentHook({}))
      if (!result) {
        return
      }
    }
    removeExitHook()
  }
  navigateTo(href, e)
}

let makeExitHook = (msg) => (e) => {
  e.returnValue = msg     // Gecko and Trident
  return msg              // Gecko and WebKit
}
