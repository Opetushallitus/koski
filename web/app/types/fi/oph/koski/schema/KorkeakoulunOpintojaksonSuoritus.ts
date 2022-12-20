import { KorkeakoulunArviointi } from './KorkeakoulunArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { KorkeakoulunOpintojakso } from './KorkeakoulunOpintojakso'
import { Oppilaitos } from './Oppilaitos'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * KorkeakoulunOpintojaksonSuoritus
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpintojaksonSuoritus`
 */
export type KorkeakoulunOpintojaksonSuoritus = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpintojaksonSuoritus'
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulunopintojakso'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: KorkeakoulunOpintojakso
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const KorkeakoulunOpintojaksonSuoritus = (o: {
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulunopintojakso'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: KorkeakoulunOpintojakso
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}): KorkeakoulunOpintojaksonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulunopintojakso',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.KorkeakoulunOpintojaksonSuoritus',
  ...o
})

export const isKorkeakoulunOpintojaksonSuoritus = (
  a: any
): a is KorkeakoulunOpintojaksonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpintojaksonSuoritus'
