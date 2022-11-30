import { navigateTo } from './location'
let currentHook = null

export const addExitHook = (msg) => {
  removeExitHook()
  if (!window.DISABLE_EXIT_HOOKS) {
    currentHook = makeExitHook(msg)
    window.addEventListener('beforeunload', currentHook)
  }
}

export const removeExitHook = () => {
  if (currentHook) {
    window.removeEventListener('beforeunload', currentHook)
    currentHook = null
  }
}

export const checkExitHook = () => {
  if (currentHook) {
    return confirm(currentHook({}))
  }
  return true
}

export const withExitHook =
  (f, useExitHook = true) =>
  (e) => {
    if (useExitHook) {
      if (currentHook) {
        const result = confirm(currentHook({}))
        if (!result) {
          if (e) e.preventDefault()
          return
        }
      }
      removeExitHook()
    }
    return f(e)
  }

export const navigateWithExitHook = (href, useExitHook = true) =>
  withExitHook((e) => navigateTo(href, e), useExitHook)

const makeExitHook = (msg) => (e) => {
  e.returnValue = msg // Gecko and Trident
  return msg // Gecko and WebKit
}
