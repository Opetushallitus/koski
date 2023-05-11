import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Oppisopimus } from './Oppisopimus'

/**
 * OppisopimuksellinenOsaamisenHankkimistapa
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksellinenOsaamisenHankkimistapa`
 */
export type OppisopimuksellinenOsaamisenHankkimistapa = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksellinenOsaamisenHankkimistapa'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  oppisopimus: Oppisopimus
}

export const OppisopimuksellinenOsaamisenHankkimistapa = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  oppisopimus: Oppisopimus
}): OppisopimuksellinenOsaamisenHankkimistapa => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksellinenOsaamisenHankkimistapa',
  ...o
})

OppisopimuksellinenOsaamisenHankkimistapa.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksellinenOsaamisenHankkimistapa' as const

export const isOppisopimuksellinenOsaamisenHankkimistapa = (
  a: any
): a is OppisopimuksellinenOsaamisenHankkimistapa =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksellinenOsaamisenHankkimistapa'
