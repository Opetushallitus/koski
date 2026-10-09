// Selaimen osoitteen ainoa omistaja. Vanhan käyttöliittymän (navigateTo,
// locationP) ja uuden käyttöliittymän (pushLocation, useSearchParam)
// navigointi kulkee tämän kautta, joten kumpikin näkee toisenkin muutokset, ja
// tallentamattomista muutoksista kysytään yhdessä paikassa.

export type Varoitus = {
  viesti: string
  // Hylkääkö siirtymä varoituksen omistajan tallentamattomat muutokset.
  hylkääMuutokset: (mistä: URL, mihin: URL) => boolean
}

const varoitukset = new Map<string, Varoitus>()
const kuuntelijat = new Set<() => void>()
let nykyinenOsoite = window.location.href

export const lisääVaroitus = (omistaja: string, varoitus: Varoitus) => {
  varoitukset.set(omistaja, varoitus)
}

export const poistaVaroitus = (omistaja: string) => {
  varoitukset.delete(omistaja)
}

export const kuunteleOsoitetta = (kuuntelija: () => void): (() => void) => {
  kuuntelijat.add(kuuntelija)
  return () => {
    kuuntelijat.delete(kuuntelija)
  }
}

// Kysyy vahvistuksen, jos siirtymä hylkäisi jonkin tallentamattomat muutokset.
// Vahvistetut varoitukset poistetaan, jotta niistä ei kysytä uudelleen.
const vahvistaSiirtymä = (mihin: string): boolean => {
  const mistäUrl = new URL(nykyinenOsoite)
  const mihinUrl = new URL(mihin, nykyinenOsoite)
  const hylättävät = Array.from(varoitukset).filter(([, varoitus]) =>
    varoitus.hylkääMuutokset(mistäUrl, mihinUrl)
  )
  if (hylättävät.length === 0) return true
  if (!window.confirm(hylättävät[0][1].viesti)) return false
  hylättävät.forEach(([omistaja]) => varoitukset.delete(omistaja))
  return true
}

const ilmoitaMuutoksesta = () =>
  kuuntelijat.forEach((kuuntelija) => kuuntelija())

// Palauttaa false, jos käyttäjä perui siirtymän.
export const siirry = (
  href: string,
  { korvaa = false }: { korvaa?: boolean } = {}
): boolean => {
  if (!vahvistaSiirtymä(href)) return false
  if (korvaa) {
    window.history.replaceState(null, '', href)
  } else {
    window.history.pushState(null, '', href)
  }
  nykyinenOsoite = window.location.href
  ilmoitaMuutoksesta()
  return true
}

// Täydentää osoitetta kertomatta kuuntelijoille, esim. kun renderöinti kirjaa
// oletuksena valitun välilehden osoitteeseen.
export const korvaaOsoiteHiljaa = (href: string) => {
  window.history.replaceState(null, '', href)
  nykyinenOsoite = window.location.href
}

window.addEventListener('popstate', () => {
  if (!vahvistaSiirtymä(window.location.href)) {
    window.history.pushState(null, '', nykyinenOsoite)
    return
  }
  nykyinenOsoite = window.location.href
  ilmoitaMuutoksesta()
})

window.addEventListener('beforeunload', (event) => {
  const varoitus = varoitukset.values().next().value
  if (varoitus) {
    event.preventDefault()
    event.returnValue = varoitus.viesti
  }
})
