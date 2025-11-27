import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { AmmatillinenPäätasonSuoritus } from './AmmatillinenPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { AmmatillinenOpiskeluoikeudenTila } from './AmmatillinenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { AmmatillisenOpiskeluoikeudenLisätiedot } from './AmmatillisenOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Ammatillisen koulutuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.AmmatillinenOpiskeluoikeus`
 */
export type AmmatillinenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ammatillinenkoulutus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<AmmatillinenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  ostettu: boolean
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: AmmatillinenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AmmatillisenOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const AmmatillinenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'ammatillinenkoulutus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<AmmatillinenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    ostettu?: boolean
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: AmmatillinenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: AmmatillisenOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): AmmatillinenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  tila: AmmatillinenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus',
  ostettu: false,
  ...o
})

AmmatillinenOpiskeluoikeus.className =
  'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus' as const

export const isAmmatillinenOpiskeluoikeus = (
  a: any
): a is AmmatillinenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeus'
