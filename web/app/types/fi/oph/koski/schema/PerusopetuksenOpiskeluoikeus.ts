import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { PerusopetuksenPäätasonSuoritus } from './PerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { PerusopetuksenOpiskeluoikeudenLisätiedot } from './PerusopetuksenOpiskeluoikeudenLisatiedot'

/**
 * Perusopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus`
 */
export type PerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'perusopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<PerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenOpiskeluoikeudenLisätiedot
}

export const PerusopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'perusopetus'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<PerusopetuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: PerusopetuksenOpiskeluoikeudenLisätiedot
  } = {}
): PerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus',
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

PerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus' as const

export const isPerusopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus'
