import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Oppisopimus } from './Oppisopimus'

/**
 * OppisopimuksellinenOsaamisenHankkimistapa
 *
 * @see `fi.oph.koski.schema.OppisopimuksellinenOsaamisenHankkimistapa`
 */
export type OppisopimuksellinenOsaamisenHankkimistapa = {
  $class: 'fi.oph.koski.schema.OppisopimuksellinenOsaamisenHankkimistapa'
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', 'oppisopimus'>
  oppisopimus: Oppisopimus
}

export const OppisopimuksellinenOsaamisenHankkimistapa = (o: {
  tunniste?: Koodistokoodiviite<'osaamisenhankkimistapa', 'oppisopimus'>
  oppisopimus: Oppisopimus
}): OppisopimuksellinenOsaamisenHankkimistapa => ({
  $class: 'fi.oph.koski.schema.OppisopimuksellinenOsaamisenHankkimistapa',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'oppisopimus',
    koodistoUri: 'osaamisenhankkimistapa'
  }),
  ...o
})

export const isOppisopimuksellinenOsaamisenHankkimistapa = (
  a: any
): a is OppisopimuksellinenOsaamisenHankkimistapa =>
  a?.$class === 'OppisopimuksellinenOsaamisenHankkimistapa'
