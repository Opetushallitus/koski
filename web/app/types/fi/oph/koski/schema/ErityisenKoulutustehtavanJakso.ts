import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskelija opiskelee erityisen koulutustehtävän mukaisesti (ib, musiikki, urheilu, kielet, luonnontieteet, jne.). Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele erityisen koulutustehtävän mukaisesti
 *
 * @see `fi.oph.koski.schema.ErityisenKoulutustehtävänJakso`
 */
export type ErityisenKoulutustehtävänJakso = {
  $class: 'fi.oph.koski.schema.ErityisenKoulutustehtävänJakso'
  alku: string
  loppu?: string
  tehtävä: Koodistokoodiviite<'erityinenkoulutustehtava', string>
}

export const ErityisenKoulutustehtävänJakso = (o: {
  alku: string
  loppu?: string
  tehtävä: Koodistokoodiviite<'erityinenkoulutustehtava', string>
}): ErityisenKoulutustehtävänJakso => ({
  $class: 'fi.oph.koski.schema.ErityisenKoulutustehtävänJakso',
  ...o
})

export const isErityisenKoulutustehtävänJakso = (
  a: any
): a is ErityisenKoulutustehtävänJakso =>
  a?.$class === 'fi.oph.koski.schema.ErityisenKoulutustehtävänJakso'
