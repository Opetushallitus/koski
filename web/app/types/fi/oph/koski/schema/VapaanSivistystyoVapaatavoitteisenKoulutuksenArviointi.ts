import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi`
 */
export type VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkovstvapaatavoitteinen' | 'arviointiasteikkovst',
    string
  >
  päivä: string
  hyväksytty?: boolean
}

export const VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkovstvapaatavoitteinen' | 'arviointiasteikkovst',
    string
  >
  päivä: string
  hyväksytty?: boolean
}): VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi',
  ...o
})

VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi.className =
  'fi.oph.koski.schema.VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi' as const

export const isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = (
  a: any
): a is VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi'
