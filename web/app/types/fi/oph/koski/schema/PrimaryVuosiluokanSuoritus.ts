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
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle: boolean
  koulutusmoduuli: PrimaryLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PrimaryOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const PrimaryVuosiluokanSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkaprimary'
  >
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: PrimaryLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PrimaryOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
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
