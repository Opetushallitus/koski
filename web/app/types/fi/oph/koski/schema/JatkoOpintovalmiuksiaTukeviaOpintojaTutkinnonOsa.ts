import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
 *
 * @see `fi.oph.koski.schema.JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa`
 */
export type JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = {
  $class: 'fi.oph.koski.schema.JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '1'>
  laajuus?: LaajuusOsaamispisteissä
}

export const JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = (
  o: {
    tunniste?: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '1'>
    laajuus?: LaajuusOsaamispisteissä
  } = {}
): JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa => ({
  $class:
    'fi.oph.koski.schema.JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa',
  tunniste: Koodistokoodiviite({
    koodiarvo: '1',
    koodistoUri: 'tutkinnonosatvalinnanmahdollisuus'
  }),
  ...o
})

export const isJatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = (
  a: any
): a is JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa =>
  a?.$class ===
  'fi.oph.koski.schema.JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'
