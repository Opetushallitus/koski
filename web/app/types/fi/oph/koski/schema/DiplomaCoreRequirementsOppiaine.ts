import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * DiplomaCoreRequirementsOppiaine
 *
 * @see `fi.oph.koski.schema.DiplomaCoreRequirementsOppiaine`
 */
export type DiplomaCoreRequirementsOppiaine = {
  $class: 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK' | 'EE' | 'CAS'>
}

export const DiplomaCoreRequirementsOppiaine = (o: {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK' | 'EE' | 'CAS'>
}): DiplomaCoreRequirementsOppiaine => ({
  $class: 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaine',
  ...o
})

export const isDiplomaCoreRequirementsOppiaine = (
  a: any
): a is DiplomaCoreRequirementsOppiaine =>
  a?.$class === 'fi.oph.koski.schema.DiplomaCoreRequirementsOppiaine'
