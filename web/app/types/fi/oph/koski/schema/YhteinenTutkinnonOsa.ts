import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Yhteisen tutkinnon osan tunnistetiedot
 *
 * @see `fi.oph.koski.schema.YhteinenTutkinnonOsa`
 */
export type YhteinenTutkinnonOsa = {
  $class: 'fi.oph.koski.schema.YhteinenTutkinnonOsa'
  tunniste: Koodistokoodiviite<
    'tutkinnonosat',
    | '101053'
    | '101054'
    | '101055'
    | '101056'
    | '106727'
    | '106728'
    | '106729'
    | '400012'
    | '400013'
    | '400014'
    | '600001'
    | '600002'
  >
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const YhteinenTutkinnonOsa = (o: {
  tunniste: Koodistokoodiviite<
    'tutkinnonosat',
    | '101053'
    | '101054'
    | '101055'
    | '101056'
    | '106727'
    | '106728'
    | '106729'
    | '400012'
    | '400013'
    | '400014'
    | '600001'
    | '600002'
  >
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): YhteinenTutkinnonOsa => ({
  $class: 'fi.oph.koski.schema.YhteinenTutkinnonOsa',
  ...o
})

YhteinenTutkinnonOsa.className =
  'fi.oph.koski.schema.YhteinenTutkinnonOsa' as const

export const isYhteinenTutkinnonOsa = (a: any): a is YhteinenTutkinnonOsa =>
  a?.$class === 'fi.oph.koski.schema.YhteinenTutkinnonOsa'
