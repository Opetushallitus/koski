import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { Koulutustoimija } from './Koulutustoimija'
import { EsiopetuksenOpiskeluoikeudenLisätiedot } from './EsiopetuksenOpiskeluoikeudenLisatiedot'
import { EsiopetuksenSuoritus } from './EsiopetuksenSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

/**
 * EsiopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus`
 */
export type EsiopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'esiopetus'>
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EsiopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  järjestämismuoto?: Koodistokoodiviite<
    'vardajarjestamismuoto',
    'JM02' | 'JM03'
  >
  suoritukset: Array<EsiopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const EsiopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'esiopetus'>
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: EsiopetuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    järjestämismuoto?: Koodistokoodiviite<
      'vardajarjestamismuoto',
      'JM02' | 'JM03'
    >
    suoritukset?: Array<EsiopetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): EsiopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'esiopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus',
  ...o
})

EsiopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus' as const

export const isEsiopetuksenOpiskeluoikeus = (
  a: any
): a is EsiopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus'
