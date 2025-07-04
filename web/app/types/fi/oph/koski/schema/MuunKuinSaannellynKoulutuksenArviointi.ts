import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * MuunKuinSäännellynKoulutuksenArviointi
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenArviointi`
 */
export type MuunKuinSäännellynKoulutuksenArviointi = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkomuks', string>
  arviointipäivä?: string
  hyväksytty?: boolean
}

export const MuunKuinSäännellynKoulutuksenArviointi = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkomuks', string>
  arviointipäivä?: string
  hyväksytty?: boolean
}): MuunKuinSäännellynKoulutuksenArviointi => ({
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenArviointi',
  ...o
})

MuunKuinSäännellynKoulutuksenArviointi.className =
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenArviointi' as const

export const isMuunKuinSäännellynKoulutuksenArviointi = (
  a: any
): a is MuunKuinSäännellynKoulutuksenArviointi =>
  a?.$class === 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenArviointi'
