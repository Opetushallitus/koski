import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * EuropeanSchoolOfHelsinkiKielioppiaineLatin
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineLatin`
 */
export type EuropeanSchoolOfHelsinkiKielioppiaineLatin = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineLatin'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'LA'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'LA'>
}

export const EuropeanSchoolOfHelsinkiKielioppiaineLatin = (o: {
  tunniste?: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'LA'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli?: Koodistokoodiviite<'kieli', 'LA'>
}): EuropeanSchoolOfHelsinkiKielioppiaineLatin => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineLatin',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'LA',
    koodistoUri: 'europeanschoolofhelsinkikielioppiaine'
  }),
  kieli: Koodistokoodiviite({ koodiarvo: 'LA', koodistoUri: 'kieli' }),
  ...o
})

EuropeanSchoolOfHelsinkiKielioppiaineLatin.className =
  'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineLatin' as const

export const isEuropeanSchoolOfHelsinkiKielioppiaineLatin = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaineLatin =>
  a?.$class === 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineLatin'
