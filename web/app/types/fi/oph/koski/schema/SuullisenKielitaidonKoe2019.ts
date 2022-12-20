import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * SuullisenKielitaidonKoe2019
 *
 * @see `fi.oph.koski.schema.SuullisenKielitaidonKoe2019`
 */
export type SuullisenKielitaidonKoe2019 = {
  $class: 'fi.oph.koski.schema.SuullisenKielitaidonKoe2019'
  päivä: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'alle_A1.1'
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
    | 'yli_C1.1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  kuvaus?: LocalizedString
}

export const SuullisenKielitaidonKoe2019 = (o: {
  päivä: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'alle_A1.1'
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
    | 'yli_C1.1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  kuvaus?: LocalizedString
}): SuullisenKielitaidonKoe2019 => ({
  $class: 'fi.oph.koski.schema.SuullisenKielitaidonKoe2019',
  ...o
})

export const isSuullisenKielitaidonKoe2019 = (
  a: any
): a is SuullisenKielitaidonKoe2019 =>
  a?.$class === 'fi.oph.koski.schema.SuullisenKielitaidonKoe2019'
