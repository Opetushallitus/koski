import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionOpiskeluoikeudenTila } from './LukionOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot } from './LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import { LukioonValmistavanKoulutuksenSuoritus } from './LukioonValmistavanKoulutuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Lukioon valmistava koulutus (LUVA)
 *
 * @see `fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus`
 */
export type LukioonValmistavanKoulutuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'luva'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<LukioonValmistavanKoulutuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const LukioonValmistavanKoulutuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'luva'>
    tila?: LukionOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<LukioonValmistavanKoulutuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): LukioonValmistavanKoulutuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'luva',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: LukionOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus',
  ...o
})

export const isLukioonValmistavanKoulutuksenOpiskeluoikeus = (
  a: any
): a is LukioonValmistavanKoulutuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.LukioonValmistavanKoulutuksenOpiskeluoikeus'
