import { VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from './VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from './VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'

/**
 * VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus`
 */
export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  {
    $class: 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
    arviointi?: Array<VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstvapaatavoitteisenkoulutuksenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
    osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  }

export const VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  (o: {
    arviointi?: Array<VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstvapaatavoitteisenkoulutuksenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
    osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  }): VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstvapaatavoitteisenkoulutuksenosasuoritus',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus',
    ...o
  })

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
