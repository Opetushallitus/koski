import { useEffect, useState } from 'react'
import { useSearchParam } from './useSearchParam'

// Sivulla saa olla muokkaustilassa vain yksi opiskeluoikeus kerrallaan, oli se
// vanhan tai uuden käyttöliittymän. Vanha käyttöliittymä pitää muokattavan
// opiskeluoikeuden osoitteessa (?edit=<oid>). Uusi käyttöliittymä ei voi
// käyttää samaa parametria, koska se käynnistää vanhan käyttöliittymän
// muokkaustilan, joten sen muokattava opiskeluoikeus pidetään tässä.
let uudenKäyttöliittymänMuokattava: string | undefined
const kuuntelijat = new Set<() => void>()

const ilmoitaMuutoksesta = () =>
  kuuntelijat.forEach((kuuntelija) => kuuntelija())

// Palauttaa funktion, joka päättää muokkauksen.
export const aloitaUudenKäyttöliittymänMuokkaus = (
  opiskeluoikeusOid: string
): (() => void) => {
  uudenKäyttöliittymänMuokattava = opiskeluoikeusOid
  ilmoitaMuutoksesta()
  return () => {
    if (uudenKäyttöliittymänMuokattava === opiskeluoikeusOid) {
      uudenKäyttöliittymänMuokattava = undefined
      ilmoitaMuutoksesta()
    }
  }
}

export const useMuokattavaOpiskeluoikeus = (): string | undefined => {
  const vanhanKäyttöliittymänMuokattava = useSearchParam('edit')
  const [uudenMuokattava, setUudenMuokattava] = useState(
    uudenKäyttöliittymänMuokattava
  )
  useEffect(() => {
    const päivitä = () => setUudenMuokattava(uudenKäyttöliittymänMuokattava)
    kuuntelijat.add(päivitä)
    päivitä()
    return () => {
      kuuntelijat.delete(päivitä)
    }
  }, [])
  return vanhanKäyttöliittymänMuokattava || uudenMuokattava
}
