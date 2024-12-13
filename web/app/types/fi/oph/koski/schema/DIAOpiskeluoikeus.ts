import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAOpiskeluoikeudenTila } from './DIAOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { DIAOpiskeluoikeudenLisätiedot } from './DIAOpiskeluoikeudenLisatiedot'
import { DIAPäätasonSuoritus } from './DIAPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'

/**
 * Deutsche Internationale Abitur -tutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeus`
 */
export type DIAOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
  tila: DIAOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: DIAOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<DIAPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
}

export const DIAOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
    tila?: DIAOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: DIAOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<DIAPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
  } = {}
): DIAOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diatutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: DIAOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus',
  ...o
})

DIAOpiskeluoikeus.className = 'fi.oph.koski.schema.DIAOpiskeluoikeus' as const

export const isDIAOpiskeluoikeus = (a: any): a is DIAOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.DIAOpiskeluoikeus'
