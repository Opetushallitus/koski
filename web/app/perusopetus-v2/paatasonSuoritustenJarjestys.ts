/**
 * Päätason suoritusten järjestys välilehtirivillä: oppimäärän suoritus
 * (päättötodistus) ensin, sitten vuosiluokat laskevassa järjestyksessä
 * (9, 8, 7...).
 *
 * Saman vuosiluokan suoritukset — esimerkiksi luokalle jäänyt ja sen uusinta —
 * järjestetään alkamispäivän mukaan uusimmasta vanhimpaan, jolloin koko rivi
 * luetaan uusimmasta vanhimpaan. Ilman tätä saman vuosiluokan suoritusten
 * keskinäinen järjestys jäi sen varaan, missä järjestyksessä ne sattuivat
 * olemaan tallennettuna.
 *
 * Vanha käyttöliittymä järjestää suoritukset palvelimella
 * (OppijaEditorModel.perusopetuksenSuoritustenJärjestysKriteeri). Sielläkin
 * alkamispäivä on laskeva, mutta sitä ennen verrataan suorituksen valmiutta,
 * jolloin kesken oleva suoritus nousee valmiin edelle päivistä riippumatta.
 * Tässä järjestys on tarkoituksella pelkän alkamispäivän varassa.
 */

type JärjestettäväPäätasonSuoritus = {
  koulutusmoduuli: { tunniste: { koodiarvo: string } }
  alkamispäivä?: string
}

export const sortPäätasonSuoritukset = <
  T extends JärjestettäväPäätasonSuoritus
>(
  suoritukset: T[],
  isOppimääränSuoritus: (suoritus: T) => boolean
): T[] =>
  [...suoritukset].sort((a, b) => {
    const aOppimäärä = isOppimääränSuoritus(a)
    const bOppimäärä = isOppimääränSuoritus(b)
    if (aOppimäärä !== bOppimäärä) {
      return aOppimäärä ? -1 : 1
    }

    const aKoodi = Number(a.koulutusmoduuli.tunniste.koodiarvo) || 0
    const bKoodi = Number(b.koulutusmoduuli.tunniste.koodiarvo) || 0
    if (aKoodi !== bKoodi) {
      return bKoodi - aKoodi
    }

    return vertaaAlkamispäivääLaskevasti(a.alkamispäivä, b.alkamispäivä)
  })

// Alkamispäivä on ISO-muotoinen (YYYY-MM-DD), joten merkkijonovertailu riittää.
// Alkamispäivätön suoritus jää viimeiseksi.
const vertaaAlkamispäivääLaskevasti = (
  a: string | undefined,
  b: string | undefined
): number => {
  if (a === b) return 0
  if (a === undefined) return 1
  if (b === undefined) return -1
  return a < b ? 1 : -1
}
