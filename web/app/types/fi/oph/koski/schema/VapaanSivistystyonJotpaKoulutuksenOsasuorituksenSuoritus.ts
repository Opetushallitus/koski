import { VapaanSivistystyöJotpaKoulutuksenArviointi } from './VapaanSivistystyoJotpaKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from './VapaanSivistystyonJotpaKoulutuksenOsasuoritus'

/**
 * VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus`
 */
export type VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus'
  arviointi?: Array<VapaanSivistystyöJotpaKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstjotpakoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
}

export const VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = (o: {
  arviointi?: Array<VapaanSivistystyöJotpaKoulutuksenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstjotpakoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
}): VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vstjotpakoulutuksenosasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus',
  ...o
})

export const isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus'
