import { EuropeanSchoolOfHelsinkiOsasuoritusArviointi } from './EuropeanSchoolOfHelsinkiOsasuoritusArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PrimarySuorituskielenVaativaOppimisalue } from './PrimarySuorituskielenVaativaOppimisalue'
import { PrimaryOppimisalueenAlaosasuoritus } from './PrimaryOppimisalueenAlaosasuoritus'

/**
 * PrimaryOppimisalueenSuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryOppimisalueenSuoritus`
 */
export type PrimaryOppimisalueenSuoritus = {
  $class: 'fi.oph.koski.schema.PrimaryOppimisalueenSuoritus'
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue
  osasuoritukset?: Array<PrimaryOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä: boolean
}

export const PrimaryOppimisalueenSuoritus = (o: {
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue
  osasuoritukset?: Array<PrimaryOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}): PrimaryOppimisalueenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkiosasuoritusprimary',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PrimaryOppimisalueenSuoritus',
  yksilöllistettyOppimäärä: false,
  ...o
})

PrimaryOppimisalueenSuoritus.className =
  'fi.oph.koski.schema.PrimaryOppimisalueenSuoritus' as const

export const isPrimaryOppimisalueenSuoritus = (
  a: any
): a is PrimaryOppimisalueenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PrimaryOppimisalueenSuoritus'
