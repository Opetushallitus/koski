import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * LukutaitokoulutuksenArviointi
 *
 * @see `fi.oph.koski.schema.LukutaitokoulutuksenArviointi`
 */
export type LukutaitokoulutuksenArviointi = {
  $class: 'fi.oph.koski.schema.LukutaitokoulutuksenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
  hyväksytty?: boolean
}

export const LukutaitokoulutuksenArviointi = (o: {
  arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
  hyväksytty?: boolean
}): LukutaitokoulutuksenArviointi => ({
  $class: 'fi.oph.koski.schema.LukutaitokoulutuksenArviointi',
  arvosana: Koodistokoodiviite({
    koodiarvo: 'Hyväksytty',
    koodistoUri: 'arviointiasteikkovst'
  }),
  ...o
})

export const isLukutaitokoulutuksenArviointi = (
  a: any
): a is LukutaitokoulutuksenArviointi =>
  a?.$class === 'LukutaitokoulutuksenArviointi'
