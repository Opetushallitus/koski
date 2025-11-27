import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { LukionPäätasonSuoritus } from './LukionPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { LukionOpiskeluoikeudenLisätiedot } from './LukionOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Lukion opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.LukionOpiskeluoikeus`
 */
export type LukionOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<LukionPäätasonSuoritus>
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  oppimääräSuoritettu?: boolean
  aikaleima?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const LukionOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<LukionPäätasonSuoritus>
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukionOpiskeluoikeudenLisätiedot
    oppimääräSuoritettu?: boolean
    aikaleima?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): LukionOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukiokoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeus',
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

LukionOpiskeluoikeus.className =
  'fi.oph.koski.schema.LukionOpiskeluoikeus' as const

export const isLukionOpiskeluoikeus = (a: any): a is LukionOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.LukionOpiskeluoikeus'
