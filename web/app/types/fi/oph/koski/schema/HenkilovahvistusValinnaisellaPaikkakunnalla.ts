import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'
import { Organisaatiohenkilö } from './Organisaatiohenkilo'

/**
 * Suorituksen vahvistus organisaatio- ja henkilötiedoilla
 *
 * @see `fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla`
 */
export type HenkilövahvistusValinnaisellaPaikkakunnalla = {
  $class: 'fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla'
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export const HenkilövahvistusValinnaisellaPaikkakunnalla = (o: {
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt?: Array<Organisaatiohenkilö>
}): HenkilövahvistusValinnaisellaPaikkakunnalla => ({
  $class: 'fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla',
  myöntäjäHenkilöt: [],
  ...o
})

HenkilövahvistusValinnaisellaPaikkakunnalla.className =
  'fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla' as const

export const isHenkilövahvistusValinnaisellaPaikkakunnalla = (
  a: any
): a is HenkilövahvistusValinnaisellaPaikkakunnalla =>
  a?.$class ===
  'fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla'
