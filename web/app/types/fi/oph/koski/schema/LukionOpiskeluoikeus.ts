import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { LukionOpiskeluoikeudenLisätiedot } from './LukionOpiskeluoikeudenLisatiedot'
import { LukionPäätasonSuoritus } from './LukionPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Lukion opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.LukionOpiskeluoikeus`
 */
export type LukionOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<LukionPäätasonSuoritus>
  oppimääräSuoritettu?: boolean
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const LukionOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukionOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<LukionPäätasonSuoritus>
    oppimääräSuoritettu?: boolean
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): LukionOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukiokoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeus',
  ...o
})

export const isLukionOpiskeluoikeus = (a: any): a is LukionOpiskeluoikeus =>
  a?.$class === 'LukionOpiskeluoikeus'
