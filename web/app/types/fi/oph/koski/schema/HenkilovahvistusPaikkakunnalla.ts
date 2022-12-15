import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'
import { Organisaatiohenkilö } from './Organisaatiohenkilo'

/**
 * Suorituksen vahvistus organisaatio- ja henkilötiedoilla
 *
 * @see `fi.oph.koski.schema.HenkilövahvistusPaikkakunnalla`
 */
export type HenkilövahvistusPaikkakunnalla = {
  $class: 'fi.oph.koski.schema.HenkilövahvistusPaikkakunnalla'
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export const HenkilövahvistusPaikkakunnalla = (o: {
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt?: Array<Organisaatiohenkilö>
}): HenkilövahvistusPaikkakunnalla => ({
  $class: 'fi.oph.koski.schema.HenkilövahvistusPaikkakunnalla',
  myöntäjäHenkilöt: [],
  ...o
})

export const isHenkilövahvistusPaikkakunnalla = (
  a: any
): a is HenkilövahvistusPaikkakunnalla =>
  a?.$class === 'HenkilövahvistusPaikkakunnalla'
