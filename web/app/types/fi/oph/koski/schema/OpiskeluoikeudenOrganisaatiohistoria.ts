import { Oppilaitos } from './Oppilaitos'
import { Koulutustoimija } from './Koulutustoimija'

/**
 * OpiskeluoikeudenOrganisaatiohistoria
 *
 * @see `fi.oph.koski.schema.OpiskeluoikeudenOrganisaatiohistoria`
 */
export type OpiskeluoikeudenOrganisaatiohistoria = {
  $class: 'fi.oph.koski.schema.OpiskeluoikeudenOrganisaatiohistoria'
  muutospäivä: string
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
}

export const OpiskeluoikeudenOrganisaatiohistoria = (o: {
  muutospäivä: string
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
}): OpiskeluoikeudenOrganisaatiohistoria => ({
  $class: 'fi.oph.koski.schema.OpiskeluoikeudenOrganisaatiohistoria',
  ...o
})

export const isOpiskeluoikeudenOrganisaatiohistoria = (
  a: any
): a is OpiskeluoikeudenOrganisaatiohistoria =>
  a?.$class === 'OpiskeluoikeudenOrganisaatiohistoria'