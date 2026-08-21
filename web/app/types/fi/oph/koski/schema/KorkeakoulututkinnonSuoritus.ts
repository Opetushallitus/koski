import { KorkeakoulunArviointi } from './KorkeakoulunArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Korkeakoulututkinto } from './Korkeakoulututkinto'
import { Oppilaitos } from './Oppilaitos'
import { KorkeakoulunOpintojaksonSuoritus } from './KorkeakoulunOpintojaksonSuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'
import { KorkeakoulunKoulutusala } from './KorkeakoulunKoulutusala'

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
  hyväksilukupäivä?: string
  lisätieto?: LocalizedString
  koulutusmoduuli: Korkeakoulututkinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusala?: KorkeakoulunKoulutusala
}

export const KorkeakoulututkinnonSuoritus = (o: {
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulututkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  hyväksilukupäivä?: string
  lisätieto?: LocalizedString
  koulutusmoduuli: Korkeakoulututkinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusala?: KorkeakoulunKoulutusala
}): KorkeakoulututkinnonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'korkeakoulututkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.KorkeakoulututkinnonSuoritus',
  ...o
})

KorkeakoulututkinnonSuoritus.className =
  'fi.oph.koski.schema.KorkeakoulututkinnonSuoritus' as const

export const isKorkeakoulututkinnonSuoritus = (
  a: any
): a is KorkeakoulututkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulututkinnonSuoritus'
