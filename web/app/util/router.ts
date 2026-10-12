// Selaimen osoitteen ainoa omistaja. Vanhan käyttöliittymän (navigateTo,
// locationP) ja uuden käyttöliittymän (LinkButton, useSearchParam)
// navigointi kulkee tämän kautta, joten kumpikin näkee toisenkin muutokset, ja
// tallentamattomista muutoksista kysytään yhdessä paikassa.

export type NavigationGuard = {
  message: string
  // Hylkääkö siirtymä varoituksen omistajan tallentamattomat muutokset.
  discardsChanges: (from: URL, to: URL) => boolean
}

const guards = new Map<string, NavigationGuard>()
const listeners = new Set<() => void>()
let currentHref = window.location.href

export const addNavigationGuard = (owner: string, guard: NavigationGuard) => {
  guards.set(owner, guard)
}

export const removeNavigationGuard = (owner: string) => {
  guards.delete(owner)
}

export const onLocationChange = (listener: () => void): (() => void) => {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

// Kysyy vahvistuksen, jos siirtymä hylkäisi jonkin tallentamattomat muutokset.
// Vahvistetut varoitukset poistetaan, jotta niistä ei kysytä uudelleen.
const confirmNavigation = (href: string): boolean => {
  const from = new URL(currentHref)
  const to = new URL(href, currentHref)
  const discarding = Array.from(guards).filter(([, guard]) =>
    guard.discardsChanges(from, to)
  )
  if (discarding.length === 0) return true
  if (!window.confirm(discarding[0][1].message)) return false
  discarding.forEach(([owner]) => guards.delete(owner))
  return true
}

const notifyListeners = () => listeners.forEach((listener) => listener())

// Palauttaa false, jos käyttäjä perui siirtymän.
export const navigate = (
  href: string,
  { replace = false }: { replace?: boolean } = {}
): boolean => {
  if (!confirmNavigation(href)) return false
  if (replace) {
    window.history.replaceState(null, '', href)
  } else {
    window.history.pushState(null, '', href)
  }
  currentHref = window.location.href
  notifyListeners()
  return true
}

// Täydentää osoitetta kertomatta kuuntelijoille, esim. kun renderöinti kirjaa
// oletuksena valitun välilehden osoitteeseen.
export const replaceLocationSilently = (href: string) => {
  window.history.replaceState(null, '', href)
  currentHref = window.location.href
}

window.addEventListener('popstate', () => {
  if (!confirmNavigation(window.location.href)) {
    window.history.pushState(null, '', currentHref)
    return
  }
  currentHref = window.location.href
  notifyListeners()
})

window.addEventListener('beforeunload', (event) => {
  const guard = guards.values().next().value
  if (guard) {
    event.preventDefault()
    event.returnValue = guard.message
  }
})
