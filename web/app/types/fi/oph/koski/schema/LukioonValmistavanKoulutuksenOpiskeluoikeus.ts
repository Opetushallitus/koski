import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { LukioonValmistavanKoulutuksenSuoritus } from './LukioonValmistavanKoulutuksenSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot } from './LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Lukioon valmistava koulutus (LUVA)
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus`
 */
export type LukioonValmistavanKoulutuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'luva'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  versionumero?: number
  suoritukset: Array<LukioonValmistavanKoulutuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const LukioonValmistavanKoulutuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'luva'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    versionumero?: number
    suoritukset?: Array<LukioonValmistavanKoulutuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): LukioonValmistavanKoulutuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luva',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus',
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

LukioonValmistavanKoulutuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus' as const

export const isLukioonValmistavanKoulutuksenOpiskeluoikeus = (
  a: any
): a is LukioonValmistavanKoulutuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus'
