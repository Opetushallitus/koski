import { Oppilaitos } from './Oppilaitos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Opiskeluoikeus, johon tämä opiskeluoikeus antaa mahdollisuuden jatkaa
 *
 * @see `fi.oph.koski.schema.LiittyväOpiskeluoikeus`
 */
export type LiittyväOpiskeluoikeus = {
  $class: 'fi.oph.koski.schema.LiittyväOpiskeluoikeus'
  lähdejärjestelmänId: string
  oppilaitos?: Oppilaitos
  tyyppi?: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
}

export const LiittyväOpiskeluoikeus = (o: {
  lähdejärjestelmänId: string
  oppilaitos?: Oppilaitos
  tyyppi?: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
}): LiittyväOpiskeluoikeus => ({
  $class: 'fi.oph.koski.schema.LiittyväOpiskeluoikeus',
  ...o
})

LiittyväOpiskeluoikeus.className =
  'fi.oph.koski.schema.LiittyväOpiskeluoikeus' as const

export const isLiittyväOpiskeluoikeus = (a: any): a is LiittyväOpiskeluoikeus =>
  a?.$class === 'fi.oph.koski.schema.LiittyväOpiskeluoikeus'
