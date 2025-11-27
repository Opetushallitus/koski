import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { PerusopetuksenLisäopetuksenSuoritus } from './PerusopetuksenLisaopetuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot } from './PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'

/**
 * Perusopetuksen lisäopetuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus`
 */
export type PerusopetuksenLisäopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'perusopetuksenlisaopetus'
  >
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<PerusopetuksenLisäopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
}

export const PerusopetuksenLisäopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'perusopetuksenlisaopetus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<PerusopetuksenLisäopetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  } = {}
): PerusopetuksenLisäopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenlisaopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus',
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

PerusopetuksenLisäopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus' as const

export const isPerusopetuksenLisäopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus'
