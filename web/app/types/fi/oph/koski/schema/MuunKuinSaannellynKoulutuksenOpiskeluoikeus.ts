import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuunKuinSäännellynKoulutuksenTila } from './MuunKuinSaannellynKoulutuksenTila'
import { OpiskeluoikeudenOrganisaatiohistoria } from './OpiskeluoikeudenOrganisaatiohistoria'
import { Koulutustoimija } from './Koulutustoimija'
import { MuunKuinSäännellynKoulutuksenPäätasonSuoritus } from './MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { LähdejärjestelmäId } from './LahdejarjestelmaId'
import { Oppilaitos } from './Oppilaitos'

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
  tila: MuunKuinSäännellynKoulutuksenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export const MuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<
      'opiskeluoikeudentyyppi',
      'muukuinsaanneltykoulutus'
    >
    tila?: MuunKuinSäännellynKoulutuksenTila
    alkamispäivä?: string
    organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
    aikaleima?: string
    päättymispäivä?: string
    lähdejärjestelmänId?: LähdejärjestelmäId
    arvioituPäättymispäivä?: string
    oppilaitos?: Oppilaitos
  } = {}
): MuunKuinSäännellynKoulutuksenOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muukuinsaanneltykoulutus',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  tila: MuunKuinSäännellynKoulutuksenTila({ opiskeluoikeusjaksot: [] }),
  suoritukset: [],
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus',
  ...o
})

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
