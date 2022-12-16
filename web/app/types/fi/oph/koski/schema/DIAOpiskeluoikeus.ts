import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { DIAOpiskeluoikeudenTila } from './DIAOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { DIAOpiskeluoikeudenLisätiedot } from './DIAOpiskeluoikeudenLisatiedot'
import { DIAPäätasonSuoritus } from './DIAPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * Deutsche Internationale Abitur -tutkinnon opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.DIAOpiskeluoikeus`
 */
export type DIAOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
  tila: DIAOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: DIAOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<DIAPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const DIAOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
    tila?: DIAOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: DIAOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<DIAPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): DIAOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'diatutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: DIAOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.DIAOpiskeluoikeus',
  ...o
})

export const isDIAOpiskeluoikeus = (a: any): a is DIAOpiskeluoikeus =>
  a?.$class === 'DIAOpiskeluoikeus'
