import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LukiodiplominSuoritusJaArviointi
 *
 * @see `fi.oph.koski.schema.LukiodiplominSuoritusJaArviointi`
 */
export type LukiodiplominSuoritusJaArviointi = {
  $class: 'fi.oph.koski.schema.LukiodiplominSuoritusJaArviointi'
  päivä: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  tunniste: Koodistokoodiviite<
    'moduulikoodistolops2021',
    | 'KOLD1'
    | 'KULD2'
    | 'KÄLD3'
    | 'LILD4'
    | 'MELD5'
    | 'MULD6'
    | 'TALD7'
    | 'TELD8'
  >
}

export const LukiodiplominSuoritusJaArviointi = (o: {
  päivä: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  tunniste: Koodistokoodiviite<
    'moduulikoodistolops2021',
    | 'KOLD1'
    | 'KULD2'
    | 'KÄLD3'
    | 'LILD4'
    | 'MELD5'
    | 'MULD6'
    | 'TALD7'
    | 'TELD8'
  >
}): LukiodiplominSuoritusJaArviointi => ({
  $class: 'fi.oph.koski.schema.LukiodiplominSuoritusJaArviointi',
  ...o
})

LukiodiplominSuoritusJaArviointi.className =
  'fi.oph.koski.schema.LukiodiplominSuoritusJaArviointi' as const

export const isLukiodiplominSuoritusJaArviointi = (
  a: any
): a is LukiodiplominSuoritusJaArviointi =>
  a?.$class === 'fi.oph.koski.schema.LukiodiplominSuoritusJaArviointi'
