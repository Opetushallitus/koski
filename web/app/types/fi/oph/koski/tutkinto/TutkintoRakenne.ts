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
  diaarinumero: string
  id: number
  suoritustavat: Array<SuoritustapaJaRakenne>
  osaamisalat: Array<Koodistokoodiviite>
  koulutustyyppi: Koodistokoodiviite
  koulutukset: Array<Koodistokoodiviite>
  voimassaoloAlkaa?: string
}

export const TutkintoRakenne = (o: {
  tutkintonimikkeet?: Array<Koodistokoodiviite>
  diaarinumero: string
  id: number
  suoritustavat?: Array<SuoritustapaJaRakenne>
  osaamisalat?: Array<Koodistokoodiviite>
  koulutustyyppi: Koodistokoodiviite
  koulutukset?: Array<Koodistokoodiviite>
  voimassaoloAlkaa?: string
}): TutkintoRakenne => ({
  tutkintonimikkeet: [],
  suoritustavat: [],
  osaamisalat: [],
  $class: 'fi.oph.koski.tutkinto.TutkintoRakenne',
  koulutukset: [],
  ...o
})

TutkintoRakenne.className = 'fi.oph.koski.tutkinto.TutkintoRakenne' as const

export const isTutkintoRakenne = (a: any): a is TutkintoRakenne =>
  a?.$class === 'fi.oph.koski.tutkinto.TutkintoRakenne'
