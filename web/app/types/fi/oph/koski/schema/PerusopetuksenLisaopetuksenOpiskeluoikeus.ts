import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot } from './PerusopetuksenLisaopetuksenOpiskeluoikeudenLisatiedot'
import { PerusopetuksenLisäopetuksenSuoritus } from './PerusopetuksenLisaopetuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

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
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<PerusopetuksenLisäopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export const PerusopetuksenLisäopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'perusopetuksenlisaopetus'
    >
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<PerusopetuksenLisäopetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    oppilaitos?: Oppilaitos
  } = {}
): PerusopetuksenLisäopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenlisaopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus',
  ...o
})

export const isPerusopetuksenLisäopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenLisäopetuksenOpiskeluoikeus'
