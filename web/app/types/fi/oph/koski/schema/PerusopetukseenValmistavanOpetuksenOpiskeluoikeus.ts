import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila } from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { PerusopetukseenValmistavanOpetuksenSuoritus } from './PerusopetukseenValmistavanOpetuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

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
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<PerusopetukseenValmistavanOpetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'perusopetukseenvalmistavaopetus'
    >
    tila?: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<PerusopetukseenValmistavanOpetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitos?: Oppilaitos
  } = {}
): PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetukseenvalmistavaopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila({
    opiskeluoikeusjaksot: []
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus',
  ...o
})

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
