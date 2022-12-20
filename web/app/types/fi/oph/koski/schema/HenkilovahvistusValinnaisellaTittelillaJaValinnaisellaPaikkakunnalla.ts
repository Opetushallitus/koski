import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Organisaatio } from './Organisaatio'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from './OrganisaatiohenkiloValinnaisellaTittelilla'

/**
 * Suorituksen vahvistus organisaatio- ja henkilötiedoilla
 *
 * @see `fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla`
 */
export type HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  {
    $class: 'fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla'
    päivä: string
    paikkakunta?: Koodistokoodiviite<'kunta', string>
    myöntäjäOrganisaatio: Organisaatio
    myöntäjäHenkilöt: Array<OrganisaatiohenkilöValinnaisellaTittelillä>
  }

export const HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  (o: {
    päivä: string
    paikkakunta?: Koodistokoodiviite<'kunta', string>
    myöntäjäOrganisaatio: Organisaatio
    myöntäjäHenkilöt?: Array<OrganisaatiohenkilöValinnaisellaTittelillä>
  }): HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla => ({
    $class:
      'fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla',
    myöntäjäHenkilöt: [],
    ...o
  })

export const isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  (
    a: any
  ): a is HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =>
    a?.$class ===
    'fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla'
