import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { IBPäätasonSuoritus } from './IBPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { LukionOpiskeluoikeudenLisätiedot } from './LukionOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * IB-tutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.IBOpiskeluoikeus`
 */
export type IBOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.IBOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibtutkinto'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<IBPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const IBOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibtutkinto'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<IBPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukionOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): IBOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibtutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.IBOpiskeluoikeus',
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

IBOpiskeluoikeus.className = 'fi.oph.koski.schema.IBOpiskeluoikeus' as const

export const isIBOpiskeluoikeus = (a: any): a is IBOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.IBOpiskeluoikeus'
