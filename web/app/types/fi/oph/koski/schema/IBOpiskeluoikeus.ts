import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { LukionOpiskeluoikeudenLisätiedot } from './LukionOpiskeluoikeudenLisatiedot'
import { IBPäätasonSuoritus } from './IBPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'

/**
 * IB-tutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.IBOpiskeluoikeus`
 */
export type IBOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.IBOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibtutkinto'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<IBPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
}

export const IBOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibtutkinto'>
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukionOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<IBPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
  } = {}
): IBOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibtutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.IBOpiskeluoikeus',
  ...o
})

IBOpiskeluoikeus.className = 'fi.oph.koski.schema.IBOpiskeluoikeus' as const

export const isIBOpiskeluoikeus = (a: any): a is IBOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.IBOpiskeluoikeus'
