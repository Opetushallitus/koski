import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { SisältäväOpiskeluoikeus } from './SisaltavaOpiskeluoikeus'
import { EsiopetuksenSuoritus } from './EsiopetuksenSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { NuortenPerusopetuksenOpiskeluoikeudenTila } from './NuortenPerusopetuksenOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { EsiopetuksenOpiskeluoikeudenLisätiedot } from './EsiopetuksenOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * EsiopetuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus`
 */
export type EsiopetuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'esiopetus'>
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  suoritukset: Array<EsiopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EsiopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  järjestämismuoto?: Koodistokoodiviite<
    'vardajarjestamismuoto',
    'JM02' | 'JM03'
  >
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const EsiopetuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'esiopetus'>
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
    oid?: string
    suoritukset?: Array<EsiopetuksenSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: NuortenPerusopetuksenOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: EsiopetuksenOpiskeluoikeudenLisätiedot
    versionumero?: number
    järjestämismuoto?: Koodistokoodiviite<
      'vardajarjestamismuoto',
      'JM02' | 'JM03'
    >
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): EsiopetuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'esiopetus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus',
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

EsiopetuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus' as const

export const isEsiopetuksenOpiskeluoikeus = (
  a: any
): a is EsiopetuksenOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus'
