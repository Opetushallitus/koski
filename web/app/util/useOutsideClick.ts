import { RefObject, useEffect, useLayoutEffect, useRef } from 'react'

export const useOutsideClick = (
  containerRefs: RefObject<HTMLElement>[],
  onOutsideClick: ((event: MouseEvent) => void) | undefined,
  enabled: boolean = true
) => {
  // Refit ja callback pidetään refissä, jotta kuuntelijaa ei tarvitse poistaa
  // ja lisätä uudelleen joka renderöinnillä, vaikka kutsuja antaisi joka kerta
  // uuden taulukon tai funktion. Kutsujan ei siis tarvitse käyttää useMemoa
  // tai useCallbackia. Refiä ei päivitetä renderöinnin aikana, koska Reactin
  // ohjeiden mukaan ref.currentia ei pidä muuttaa silloin. Se päivitetään
  // layout-efektissä eikä tavallisessa efektissä, koska layout-efekti ajetaan
  // heti renderöinnin jälkeen, ennen kuin selain ehtii käsitellä seuraavan
  // klikkauksen. Tavallinen efekti ajetaan vasta myöhemmin, ja sitä ennen
  // tullut klikkaus käyttäisi edellisen renderöinnin arvoja.
  const latestRef = useRef({ containerRefs, onOutsideClick })
  useLayoutEffect(() => {
    latestRef.current = { containerRefs, onOutsideClick }
  })

  const listening = enabled && onOutsideClick !== undefined
  useEffect(() => {
    if (listening) {
      const handler = (event: MouseEvent) => {
        const { target } = event
        const containers = latestRef.current.containerRefs
          .map((containerRef) => containerRef.current)
          .filter((container) => container !== null)
        if (
          target instanceof Node &&
          containers.length > 0 &&
          !containers.some((container) => container.contains(target))
        ) {
          latestRef.current.onOutsideClick?.(event)
        }
      }
      // Kuunnellaan capture-vaiheessa (true), koska silloin:
      // - Klikkaus, joka avasi containerin, ei sulje sitä heti. Kuuntelija
      //   lisätään vasta, kun klikkaus on jo ohittanut documentin
      //   capture-vaiheen.
      // - Klikattu elementti on vielä DOMissa, vaikka React-käsittelijä
      //   poistaisi sen uudelleenrenderöinnissä, joten contains() antaa
      //   oikean tuloksen.
      // - Kuuntelija saa klikkauksen, vaikka jokin komponentti kutsuisi
      //   stopPropagation()-metodia.
      document.addEventListener('click', handler, true)
      return () => document.removeEventListener('click', handler, true)
    }
  }, [listening])
}
