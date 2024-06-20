import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { SuoritustapaJaRakenne } from './SuoritustapaJaRakenne'

/**
 * TutkintoRakenne
 *
 * @see `fi.oph.koski.tutkinto.TutkintoRakenne`
 */
export type TutkintoRakenne = {
  $class: 'fi.oph.koski.tutkinto.TutkintoRakenne'
  tutkintonimikkeet: Array<Koodistokoodiviite>
  koulutukset: Array<Koodistokoodiviite>
  diaarinumero: string
  id: number
  suoritustavat: Array<SuoritustapaJaRakenne>
  osaamisalat: Array<Koodistokoodiviite>
  koulutustyyppi: Koodistokoodiviite
}

export const TutkintoRakenne = (o: {
  tutkintonimikkeet?: Array<Koodistokoodiviite>
  koulutukset?: Array<Koodistokoodiviite>
  diaarinumero: string
  id: number
  suoritustavat?: Array<SuoritustapaJaRakenne>
  osaamisalat?: Array<Koodistokoodiviite>
  koulutustyyppi: Koodistokoodiviite
}): TutkintoRakenne => ({
  tutkintonimikkeet: [],
  koulutukset: [],
  suoritustavat: [],
  osaamisalat: [],
  $class: 'fi.oph.koski.tutkinto.TutkintoRakenne',
  ...o
})

TutkintoRakenne.className = 'fi.oph.koski.tutkinto.TutkintoRakenne' as const

export const isTutkintoRakenne = (a: any): a is TutkintoRakenne =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkintoRakenne'
