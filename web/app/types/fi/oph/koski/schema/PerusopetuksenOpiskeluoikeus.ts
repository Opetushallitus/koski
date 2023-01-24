import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { PerusopetuksenOpiskeluoikeudenLisätiedot } from './PerusopetuksenOpiskeluoikeudenLisatiedot'
import { PerusopetuksenPäätasonSuoritus } from './PerusopetuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Perusopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus`
 */
export type PerusopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'perusopetus'>
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<PerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export const PerusopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'perusopetus'>
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: PerusopetuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<PerusopetuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitos?: Oppilaitos
  } = {}
): PerusopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus',
  ...o
})

PerusopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus' as const

export const isPerusopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus'
