import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila } from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot } from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisatiedot'
import { EuropeanSchoolOfHelsinkiPäätasonSuoritus } from './EuropeanSchoolOfHelsinkiPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * EuropeanSchoolOfHelsinkiOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus`
 */
export type EuropeanSchoolOfHelsinkiOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'europeanschoolofhelsinki'
  >
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<EuropeanSchoolOfHelsinkiPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const EuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'europeanschoolofhelsinki'
    >
    tila?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<EuropeanSchoolOfHelsinkiPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): EuropeanSchoolOfHelsinkiOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinki',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus',
  ...o
})

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus'
