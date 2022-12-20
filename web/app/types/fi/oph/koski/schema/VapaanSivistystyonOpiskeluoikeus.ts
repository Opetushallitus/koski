import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { VapaanSivistystyönOpiskeluoikeudenTila } from './VapaanSivistystyonOpiskeluoikeudenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { VapaanSivistystyönOpiskeluoikeudenLisätiedot } from './VapaanSivistystyonOpiskeluoikeudenLisatiedot'
import { VapaanSivistystyönPäätasonSuoritus } from './VapaanSivistystyonPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

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
  tila: VapaanSivistystyönOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: VapaanSivistystyönOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<VapaanSivistystyönPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const VapaanSivistystyönOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'vapaansivistystyonkoulutus'
    >
    tila?: VapaanSivistystyönOpiskeluoikeudenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: VapaanSivistystyönOpiskeluoikeudenLisätiedot
    versionumero?: number
    suoritukset?: Array<VapaanSivistystyönPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): VapaanSivistystyönOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'vapaansivistystyonkoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: VapaanSivistystyönOpiskeluoikeudenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus',
  ...o
})

export const isVapaanSivistystyönOpiskeluoikeus = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.VapaanSivistystyönOpiskeluoikeus'
