import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Hyväksytty arviointi tarkoittaa osasuorituksen suoritusta
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022`
 */
export type VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}

export const VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = (o: {
  arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}): VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 => ({
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'Hyväksytty',
    koodistoUri: 'arviointiasteikkovst'
  }),
  ...o
})

export const isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
