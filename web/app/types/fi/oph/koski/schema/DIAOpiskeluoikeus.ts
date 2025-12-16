import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { DIAPäätasonSuoritus } from './DIAPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { DIAOpiskeluoikeudenTila } from './DIAOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { DIAOpiskeluoikeudenLisätiedot } from './DIAOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Deutsche Internationale Abitur -tutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeus`
 */
export type DIAOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<DIAPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: DIAOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: DIAOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const DIAOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<DIAPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: DIAOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: DIAOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): DIAOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diatutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus',
  tila: DIAOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

DIAOpiskeluoikeus.className = 'fi.oph.koski.schema.DIAOpiskeluoikeus' as const

export const isDIAOpiskeluoikeus = (a: any): a is DIAOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.DIAOpiskeluoikeus'
