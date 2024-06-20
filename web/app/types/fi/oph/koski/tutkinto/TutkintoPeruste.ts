import { LocalizedString } from '../schema/LocalizedString'
import { TutkintoRakenne } from './TutkintoRakenne'

/**
 * TutkintoPeruste
 *
 * @see `fi.oph.koski.tutkinto.TutkintoPeruste`
 */
export type TutkintoPeruste = {
  $class: 'fi.oph.koski.tutkinto.TutkintoPeruste'
  diaarinumero: string
  tutkintoKoodi: string
  nimi?: LocalizedString
  rakenne?: TutkintoRakenne
}

export const TutkintoPeruste = (o: {
  diaarinumero: string
  tutkintoKoodi: string
  nimi?: LocalizedString
  rakenne?: TutkintoRakenne
}): TutkintoPeruste => ({
  $class: 'fi.oph.koski.tutkinto.TutkintoPeruste',
  ...o
})

TutkintoPeruste.className = 'fi.oph.koski.tutkinto.TutkintoPeruste' as const

export const isTutkintoPeruste = (a: any): a is TutkintoPeruste =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkintoPeruste'
