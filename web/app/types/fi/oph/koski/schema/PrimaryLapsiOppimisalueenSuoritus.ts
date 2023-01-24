import { EuropeanSchoolOfHelsinkiOsasuoritusArviointi } from './EuropeanSchoolOfHelsinkiOsasuoritusArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PrimaryLapsiOppimisalue } from './PrimaryLapsiOppimisalue'
import { PrimaryLapsiOppimisalueenAlaosasuoritus } from './PrimaryLapsiOppimisalueenAlaosasuoritus'

/**
 * PrimaryLapsiOppimisalueenSuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryLapsiOppimisalueenSuoritus`
 */
export type PrimaryLapsiOppimisalueenSuoritus = {
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalueenSuoritus'
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: PrimaryLapsiOppimisalue
  osasuoritukset?: Array<PrimaryLapsiOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä: boolean
}

export const PrimaryLapsiOppimisalueenSuoritus = (o: {
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: PrimaryLapsiOppimisalue
  osasuoritukset?: Array<PrimaryLapsiOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}): PrimaryLapsiOppimisalueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkiosasuoritusprimarylapsi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PrimaryLapsiOppimisalueenSuoritus',
  yksilöllistettyOppimäärä: false,
  ...o
})

PrimaryLapsiOppimisalueenSuoritus.className =
  'fi.oph.koski.schema.PrimaryLapsiOppimisalueenSuoritus' as const

export const isPrimaryLapsiOppimisalueenSuoritus = (
  a: any
): a is PrimaryLapsiOppimisalueenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PrimaryLapsiOppimisalueenSuoritus'
