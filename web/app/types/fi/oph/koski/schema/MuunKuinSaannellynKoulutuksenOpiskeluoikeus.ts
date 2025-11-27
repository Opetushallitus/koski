import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { MuunKuinSäännellynKoulutuksenPäätasonSuoritus } from './MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { LähdejärjestelmäkytkennänPurkaminen } from './LahdejarjestelmakytkennanPurkaminen'
import { Oppilaitos } from './Oppilaitos'
import { MuunKuinSäännellynKoulutuksenTila } from './MuunKuinSaannellynKoulutuksenTila'
import { Koulutustoimija } from './Koulutustoimija'
import { MuunKuinSäännellynKoulutuksenLisätiedot } from './MuunKuinSaannellynKoulutuksenLisatiedot'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'

/**
 * MuunKuinSäännellynKoulutuksenOpiskeluoikeus
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus`
 */
export type MuunKuinSäännellynKoulutuksenOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'muukuinsaanneltykoulutus'
  >
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  versionumero?: number
  suoritukset: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
  oppilaitos?: Oppilaitos
  tila: MuunKuinSäännellynKoulutuksenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: MuunKuinSäännellynKoulutuksenLisätiedot
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
}

export const MuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'muukuinsaanneltykoulutus'
    >
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    versionumero?: number
    suoritukset?: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmäkytkentäPurettu?: LähdejärjestelmäkytkennänPurkaminen
    oppilaitos?: Oppilaitos
    tila?: MuunKuinSäännellynKoulutuksenTila
    alkamispäivä?: string
    koulutustoimija?: Koulutustoimija
    lisätiedot?: MuunKuinSäännellynKoulutuksenLisätiedot
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
  } = {}
): MuunKuinSäännellynKoulutuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muukuinsaanneltykoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus',
  tila: MuunKuinSäännellynKoulutuksenTila({ opiskeluoikeusjaksot: [] }),
  ...o
})

MuunKuinSäännellynKoulutuksenOpiskeluoikeus.className =
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus' as const

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
