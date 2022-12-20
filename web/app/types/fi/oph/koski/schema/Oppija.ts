import { Henkilö } from './Henkilo'
import { Opiskeluoikeus } from './Opiskeluoikeus'

/**
 * Oppija Koski-järjestelmässä. Sisältää henkilötiedot ja listan opiskeluoikeuksista, jotka puolestaan sisältävät suorituksia, läsnäolotietoja jne. Henkilötietoja ei tallenneta Koskeen, vaan haetaan/tallennetaan Opintopolun henkilöpalveluun
 *
 * @see `fi.oph.koski.schema.Oppija`
 */
export type Oppija = {
  $class: 'fi.oph.koski.schema.Oppija'
  henkilö: Henkilö
  opiskeluoikeudet: Array<Opiskeluoikeus>
}

export const Oppija = (o: {
  henkilö: Henkilö
  opiskeluoikeudet?: Array<Opiskeluoikeus>
}): Oppija => ({
  $class: 'fi.oph.koski.schema.Oppija',
  opiskeluoikeudet: [],
  ...o
})

export const isOppija = (a: any): a is Oppija =>
  a?.$class === 'fi.oph.koski.schema.Oppija'
