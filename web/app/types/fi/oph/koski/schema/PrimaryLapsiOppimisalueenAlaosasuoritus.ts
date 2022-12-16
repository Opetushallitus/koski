import { PrimaryLapsiAlaoppimisalue } from './PrimaryLapsiAlaoppimisalue'
import { PrimaryAlaoppimisalueArviointi } from './PrimaryAlaoppimisalueArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PrimaryLapsiOppimisalueenAlaosasuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryLapsiOppimisalueenAlaosasuoritus`
 */
export type PrimaryLapsiOppimisalueenAlaosasuoritus = {
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalueenAlaosasuoritus'
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const PrimaryLapsiOppimisalueenAlaosasuoritus = (o: {
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): PrimaryLapsiOppimisalueenAlaosasuoritus => ({
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalueenAlaosasuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkialaosasuoritusprimarylapsi',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isPrimaryLapsiOppimisalueenAlaosasuoritus = (
  a: any
): a is PrimaryLapsiOppimisalueenAlaosasuoritus =>
  a?.$class === 'PrimaryLapsiOppimisalueenAlaosasuoritus'
