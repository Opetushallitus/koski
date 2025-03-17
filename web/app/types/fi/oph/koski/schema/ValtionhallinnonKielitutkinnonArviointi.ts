import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * ValtionhallinnonKielitutkinnonArviointi
 *
 * @see `fi.oph.koski.schema.ValtionhallinnonKielitutkinnonArviointi`
 */
export type ValtionhallinnonKielitutkinnonArviointi = {
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonArviointi'
  arvosana: Koodistokoodiviite<'vktarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export const ValtionhallinnonKielitutkinnonArviointi = (o: {
  arvosana: Koodistokoodiviite<'vktarvosana', string>
  päivä: string
  hyväksytty?: boolean
}): ValtionhallinnonKielitutkinnonArviointi => ({
  $class: 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonArviointi',
  ...o
})

ValtionhallinnonKielitutkinnonArviointi.className =
  'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonArviointi' as const

export const isValtionhallinnonKielitutkinnonArviointi = (
  a: any
): a is ValtionhallinnonKielitutkinnonArviointi =>
  a?.$class === 'fi.oph.koski.schema.ValtionhallinnonKielitutkinnonArviointi'
