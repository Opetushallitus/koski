import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi`
 */
export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi = {
  $class: 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi'
  arvosana: Koodistokoodiviite<
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
    | 'alle_A1.1'
    | 'yli_C1.1'
  >
  arviointipäivä?: string
  hyväksytty?: boolean
}

export const VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi = (o: {
  arvosana: Koodistokoodiviite<
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
    | 'alle_A1.1'
    | 'yli_C1.1'
  >
  arviointipäivä?: string
  hyväksytty?: boolean
}): VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi => ({
  $class:
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi',
  ...o
})

VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi.className =
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi' as const

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi =>
  a?.$class ===
  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi'
