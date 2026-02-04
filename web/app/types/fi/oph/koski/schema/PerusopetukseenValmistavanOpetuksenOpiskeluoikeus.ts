import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { PerusopetukseenValmistavanOpetuksenSuoritus } from './PerusopetukseenValmistavanOpetuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila } from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { OpiskeluoikeudenLisätiedot } from './OpiskeluoikeudenLisatiedot'

/**
 * Perusopetukseen valmistavan opetuksen opiskeluoikeuden tiedot
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus`
 */
export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<PerusopetukseenValmistavanOpetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: OpiskeluoikeudenLisätiedot
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'perusopetukseenvalmistavaopetus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<PerusopetukseenValmistavanOpetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: OpiskeluoikeudenLisätiedot
  } = {}
): PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetukseenvalmistavaopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus',
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  ...o
})

PerusopetukseenValmistavanOpetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus' as const

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
