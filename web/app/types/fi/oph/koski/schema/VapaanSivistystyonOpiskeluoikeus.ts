import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { VapaanSivistystyönPäätasonSuoritus } from './VapaanSivistystyonPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { VapaanSivistystyönOpiskeluoikeudenTila } from './VapaanSivistystyonOpiskeluoikeudenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { VapaanSivistystyönOpiskeluoikeudenLisätiedot } from './VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * Vapaan sivistystyön koulutuksen opiskeluoikeus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus`
 */
export type VapaanSivistystyönOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'vapaansivistystyonkoulutus'
  >
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset: Array<VapaanSivistystyönPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: VapaanSivistystyönOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: VapaanSivistystyönOpiskeluoikeudenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const VapaanSivistystyönOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'vapaansivistystyonkoulutus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    versionumero?: number
    suoritukset?: Array<VapaanSivistystyönPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: VapaanSivistystyönOpiskeluoikeudenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: VapaanSivistystyönOpiskeluoikeudenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): VapaanSivistystyönOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vapaansivistystyonkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus',
  tila: VapaanSivistystyönOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

VapaanSivistystyönOpiskeluoikeus.className =
  'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus' as const

export const isVapaanSivistystyönOpiskeluoikeus = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus'
