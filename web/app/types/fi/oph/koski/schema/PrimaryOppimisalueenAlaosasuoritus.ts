import { PrimaryAlaoppimisalue } from './PrimaryAlaoppimisalue'
import { PrimaryAlaoppimisalueArviointi } from './PrimaryAlaoppimisalueArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PrimaryOppimisalueenAlaosasuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryOppimisalueenAlaosasuoritus`
 */
export type PrimaryOppimisalueenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.PrimaryOppimisalueenAlaosasuoritus'
  koulutusmoduuli: PrimaryAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const PrimaryOppimisalueenAlaosasuoritus = (o: {
  koulutusmoduuli: PrimaryAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): PrimaryOppimisalueenAlaosasuoritus => ({
  $class: 'fi.oph.koski.schema.PrimaryOppimisalueenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkialaosasuoritusprimary',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isPrimaryOppimisalueenAlaosasuoritus = (
  a: any
): a is PrimaryOppimisalueenAlaosasuoritus =>
  a?.$class === 'PrimaryOppimisalueenAlaosasuoritus'
