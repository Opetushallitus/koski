import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { EuropeanSchoolOfHelsinkiPäätasonSuoritus } from './EuropeanSchoolOfHelsinkiPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila } from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot } from './EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

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
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset: Array<EuropeanSchoolOfHelsinkiPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const EuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'europeanschoolofhelsinki'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    versionumero?: number
    suoritukset?: Array<EuropeanSchoolOfHelsinkiPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): EuropeanSchoolOfHelsinkiOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'europeanschoolofhelsinki',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus',
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

EuropeanSchoolOfHelsinkiOpiskeluoikeus.className =
  'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus' as const

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus'
