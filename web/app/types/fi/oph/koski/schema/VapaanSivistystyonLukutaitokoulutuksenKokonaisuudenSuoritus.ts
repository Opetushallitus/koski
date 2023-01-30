import { VapaanSivistystyönLukutaidonKokonaisuus } from './VapaanSivistystyonLukutaidonKokonaisuus'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukutaitokoulutuksenArviointi } from './LukutaitokoulutuksenArviointi'

/**
 * VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus`
 */
export type VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus'
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstlukutaitokoulutuksenkokonaisuudensuoritus'
  >
  arviointi?: Array<LukutaitokoulutuksenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = (o: {
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstlukutaitokoulutuksenkokonaisuudensuoritus'
  >
  arviointi?: Array<LukutaitokoulutuksenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus => ({
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstlukutaitokoulutuksenkokonaisuudensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus.className =
  'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus' as const

export const isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus'
