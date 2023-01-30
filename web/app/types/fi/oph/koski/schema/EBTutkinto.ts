import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * EBTutkinto
 *
 * @see `fi.oph.koski.schema.EBTutkinto`
 */
export type EBTutkinto = {
  $class: 'fi.oph.koski.schema.EBTutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301104'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export const EBTutkinto = (o: {
  tunniste?: Koodistokoodiviite<'koulutus', '301104'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}): EBTutkinto => ({
  $class: 'fi.oph.koski.schema.EBTutkinto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '301104',
    koodistoUri: 'koulutus'
  }),
  ...o
})

EBTutkinto.className = 'fi.oph.koski.schema.EBTutkinto' as const

export const isEBTutkinto = (a: any): a is EBTutkinto =>
  a?.$class === 'fi.oph.koski.schema.EBTutkinto'
