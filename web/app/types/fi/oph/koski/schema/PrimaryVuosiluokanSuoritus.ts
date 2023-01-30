import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PrimaryLuokkaAste } from './PrimaryLuokkaAste'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { PrimaryOsasuoritus } from './PrimaryOsasuoritus'
import { HenkilövahvistusPaikkakunnalla } from './HenkilovahvistusPaikkakunnalla'

/**
 * PrimaryVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryVuosiluokanSuoritus`
 */
export type PrimaryVuosiluokanSuoritus = {
  $class: 'fi.oph.koski.schema.PrimaryVuosiluokanSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkaprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: PrimaryLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PrimaryOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export const PrimaryVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkaprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: PrimaryLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PrimaryOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}): PrimaryVuosiluokanSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinkivuosiluokkaprimary',
    koodistoUri: 'suorituksentyyppi'
  }),
  jääLuokalle: false,
  $class: 'fi.oph.koski.schema.PrimaryVuosiluokanSuoritus',
  ...o
})

PrimaryVuosiluokanSuoritus.className =
  'fi.oph.koski.schema.PrimaryVuosiluokanSuoritus' as const

export const isPrimaryVuosiluokanSuoritus = (
  a: any
): a is PrimaryVuosiluokanSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PrimaryVuosiluokanSuoritus'
