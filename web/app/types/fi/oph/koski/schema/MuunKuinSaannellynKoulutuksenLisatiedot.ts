import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * MuunKuinSäännellynKoulutuksenLisätiedot
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenLisätiedot`
 */
export type MuunKuinSäännellynKoulutuksenLisätiedot = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenLisätiedot'
  jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
}

export const MuunKuinSäännellynKoulutuksenLisätiedot = (
  o: {
    jotpaAsianumero?: Koodistokoodiviite<'jotpaasianumero', string>
  } = {}
): MuunKuinSäännellynKoulutuksenLisätiedot => ({
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenLisätiedot',
  ...o
})

MuunKuinSäännellynKoulutuksenLisätiedot.className =
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenLisätiedot' as const

export const isMuunKuinSäännellynKoulutuksenLisätiedot = (
  a: any
): a is MuunKuinSäännellynKoulutuksenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenLisätiedot'
