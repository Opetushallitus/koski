// Tyylit ladataan omana chunkkinaan, jotta CSP-nonce ehditään asettaa ennen kuin
// style-loader lisää <style>-elementit sivulle. Lataus on irrallinen promise,
// jota mikään ei yritä uudelleen, joten epäonnistuessaan sivu jää pysyvästi
// ilman tyylejä. Näin käy esimerkiksi julkaisun aikana, jos selain ehtii pyytää
// chunkkia vasta kun palvelin on jo vaihtunut uuteen versioon eikä vanhan
// nimistä tiedostoa enää ole.
//
// Sivun lataaminen uudelleen hakee tuoreen HTML:n ja sitä vastaavan chunkin
// osoitteen. Se tehdään vain kerran välilehteä kohden, jottei pysyvästi puuttuva
// tiedosto jätä sivua latautumaan loputtomiin.

const RELOAD_STORAGE_KEY = 'styleChunkReloadAttempted'

// Palauttaa true vain kerran välilehteä kohden. sessionStorage on kääritty
// try-catchiin, koska se ei ole käytettävissä kaikissa selaimen
// tallennusasetuksissa eikä virheenkäsittely saa itse kaatua. Ilman
// tallennustilaa uudelleenlatausta ei tehdä lainkaan, koska sitä ei silloin voi
// rajata yhteen kertaan.
const claimReloadAttempt = (): boolean => {
  try {
    if (sessionStorage.getItem(RELOAD_STORAGE_KEY) !== null) {
      return false
    }
    sessionStorage.setItem(RELOAD_STORAGE_KEY, '1')
    return true
  } catch (e) {
    console.warn('sessionStorage ei ole käytettävissä', e)
    return false
  }
}

export const loadStyles = (load: () => Promise<unknown>): void => {
  load().then(
    () => {
      try {
        sessionStorage.removeItem(RELOAD_STORAGE_KEY)
      } catch (e) {
        console.warn('sessionStorage ei ole käytettävissä', e)
      }
    },
    (error) => {
      console.error('Tyylien lataus epäonnistui', error)
      if (claimReloadAttempt()) {
        window.location.reload()
      }
    }
  )
}
