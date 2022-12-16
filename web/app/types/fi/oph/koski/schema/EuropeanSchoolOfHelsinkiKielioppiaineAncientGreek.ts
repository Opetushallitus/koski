import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek`
 */
export type EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'GRC'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'EL'>
}

export const EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = (o: {
  tunniste?: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'GRC'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli?: Koodistokoodiviite<'kieli', 'EL'>
}): EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek => ({
  $class:
    'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'GRC',
    koodistoUri: 'europeanschoolofhelsinkikielioppiaine'
  }),
  kieli: Koodistokoodiviite({ koodiarvo: 'EL', koodistoUri: 'kieli' }),
  ...o
})

export const isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek =>
  a?.$class === 'EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek'
