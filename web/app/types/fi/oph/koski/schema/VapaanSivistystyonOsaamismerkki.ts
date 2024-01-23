import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Vapaan sivistystyön osaamismerkin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOsaamismerkki`
 */
export type VapaanSivistystyönOsaamismerkki = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkki'
  tunniste: Koodistokoodiviite<'osaamismerkit', string>
}

export const VapaanSivistystyönOsaamismerkki = (o: {
  tunniste: Koodistokoodiviite<'osaamismerkit', string>
}): VapaanSivistystyönOsaamismerkki => ({
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkki',
  ...o
})

VapaanSivistystyönOsaamismerkki.className =
  'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkki' as const

export const isVapaanSivistystyönOsaamismerkki = (
  a: any
): a is VapaanSivistystyönOsaamismerkki =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönOsaamismerkki'
