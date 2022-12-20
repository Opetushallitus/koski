import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillinenOpiskeluoikeudenTila } from './AmmatillinenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from './AmmatillisenOpiskeluoikeudenLisatiedot'
import { AmmatillinenPäätasonSuoritus } from './AmmatillinenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Ammatillisen koulutuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AmmatillinenOpiskeluoikeus`
 */
export type AmmatillinenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ammatillinenkoulutus'>
  tila: AmmatillinenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AmmatillisenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<AmmatillinenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  ostettu: boolean
  oppilaitos?: Oppilaitos
}

export const AmmatillinenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ammatillinenkoulutus'
    >
    tila?: AmmatillinenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AmmatillisenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<AmmatillinenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    ostettu?: boolean
    oppilaitos?: Oppilaitos
  } = {}
): AmmatillinenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: AmmatillinenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus',
  ostettu: false,
  ...o
})

export const isAmmatillinenOpiskeluoikeus = (
  a: any
): a is AmmatillinenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus'
