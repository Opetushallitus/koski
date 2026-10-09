import { navigateTo } from './location'

// Varoitukset omistajittain, jotta vanha käyttöliittymä ja uuden
// käyttöliittymän editori eivät poista toistensa varoituksia.
const OLD_UI = 'old-ui'
const hooks = new Map()

export const addExitHook = (msg, owner = OLD_UI) => {
  removeExitHook(owner)
  if (!window.DISABLE_EXIT_HOOKS) {
    const hook = makeExitHook(msg)
    hooks.set(owner, hook)
    window.addEventListener('beforeunload', hook)
  }
}

export const removeExitHook = (owner = OLD_UI) => {
  const hook = hooks.get(owner)
  if (hook) {
    window.removeEventListener('beforeunload', hook)
    hooks.delete(owner)
  }
}

export const removeAllExitHooks = () => {
  Array.from(hooks.keys()).forEach((owner) => removeExitHook(owner))
}

const confirmExit = (owners) => {
  const hook = owners.map((owner) => hooks.get(owner)).find(Boolean)
  return !hook || confirm(hook({}))
}

export const checkExitHook = () => confirmExit(Array.from(hooks.keys()))

// Tarkistetaan navigoinnissa, joka poistaa uuden käyttöliittymän editorit
// (navigateTo). Vanhan käyttöliittymän omat muutokset säilyvät sen tilassa,
// joten sen varoitus tarkistetaan vain linkeissä, jotka eivät ohita sitä
// (withExitHook).
export const checkV2ExitHooks = () => {
  const owners = Array.from(hooks.keys()).filter((owner) => owner !== OLD_UI)
  if (!confirmExit(owners)) return false
  owners.forEach((owner) => removeExitHook(owner))
  return true
}

export const withExitHook =
  (f, useExitHook = true) =>
  (e) => {
    if (useExitHook) {
      if (!checkExitHook()) {
        if (e) e.preventDefault()
        return
      }
      removeAllExitHooks()
    }
    return f(e)
  }

export const navigateWithExitHook = (href, useExitHook = true) =>
  withExitHook((e) => navigateTo(href, e), useExitHook)

const makeExitHook = (msg) => (e) => {
  e.returnValue = msg // Gecko and Trident
  return msg // Gecko and WebKit
}
