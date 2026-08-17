import * as A from 'fp-ts/Array'
import * as string from 'fp-ts/string'
import { nonNull } from '../util/fp/arrays'

const distinctKoodistoUri = A.uniq(string.Eq)

export const uniqueKoodistoUris = (
  koodistoUris: Array<string | null | undefined>
): string[] => distinctKoodistoUri(koodistoUris.filter(nonNull))

/**
 * Poistaa duplikaatit id:n perusteella säilyttäen ensimmäisen esiintymän ja
 * alkuperäisen järjestyksen.
 *
 * Toteutettu Setillä eikä fp-ts:n A.uniq:lla, koska A.uniq vertailee jokaista
 * alkiota lineaarisesti kaikkiin jo hyväksyttyihin, eli on O(n²). Isoilla
 * koodistoilla (esim. "tutkinnonosat", ~11 000 koodia) tämä tarkoitti kymmeniä
 * miljoonia vertailuja joka kerta kun jokin koodisto latautui, ja käytännössä
 * useiden sekuntien jumia selaimen pääsäikeessä muokkaustilaan siirryttäessä.
 */
export const uniqueKoodistot = <T extends { id: string }>(
  koodistot: T[]
): T[] => {
  const nähdyt = new Set<string>()
  return koodistot.filter((koodi) => {
    if (nähdyt.has(koodi.id)) {
      return false
    }
    nähdyt.add(koodi.id)
    return true
  })
}
