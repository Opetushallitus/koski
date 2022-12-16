import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Näytön eri arviointikohteiden (Työprosessin hallinta jne) arvosanat
 *
 * @see `fi.oph.koski.schema.NäytönArviointikohde`
 */
export type NäytönArviointikohde = {
  $class: 'fi.oph.koski.schema.NäytönArviointikohde'
  tunniste: Koodistokoodiviite<'ammatillisennaytonarviointikohde', string>
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
}

export const NäytönArviointikohde = (o: {
  tunniste: Koodistokoodiviite<'ammatillisennaytonarviointikohde', string>
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
}): NäytönArviointikohde => ({
  $class: 'fi.oph.koski.schema.NäytönArviointikohde',
  ...o
})

export const isNäytönArviointikohde = (a: any): a is NäytönArviointikohde =>
  a?.$class === 'NäytönArviointikohde'
