import { KorkeakoulunArviointi } from './KorkeakoulunArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Korkeakoulututkinto } from './Korkeakoulututkinto'
import { Oppilaitos } from './Oppilaitos'
import { KorkeakoulunOpintojaksonSuoritus } from './KorkeakoulunOpintojaksonSuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * KorkeakoulututkinnonSuoritus
 *
 * @see `fi.oph.koski.schema.KorkeakoulututkinnonSuoritus`
 */
export type KorkeakoulututkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.KorkeakoulututkinnonSuoritus'
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulututkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Korkeakoulututkinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const KorkeakoulututkinnonSuoritus = (o: {
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulututkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Korkeakoulututkinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}): KorkeakoulututkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulututkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.KorkeakoulututkinnonSuoritus',
  ...o
})

export const isKorkeakoulututkinnonSuoritus = (
  a: any
): a is KorkeakoulututkinnonSuoritus =>
  a?.$class === 'KorkeakoulututkinnonSuoritus'
