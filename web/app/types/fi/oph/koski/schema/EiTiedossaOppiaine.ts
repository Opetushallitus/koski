import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * EiTiedossaOppiaine
 *
 * @see `fi.oph.koski.schema.EiTiedossaOppiaine`
 */
export type EiTiedossaOppiaine = {
  $class: 'fi.oph.koski.schema.EiTiedossaOppiaine'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'XX'>
  perusteenDiaarinumero?: string
}

export const EiTiedossaOppiaine = (
  o: {
    tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'XX'>
    perusteenDiaarinumero?: string
  } = {}
): EiTiedossaOppiaine => ({
  $class: 'fi.oph.koski.schema.EiTiedossaOppiaine',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'XX',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

EiTiedossaOppiaine.className = 'fi.oph.koski.schema.EiTiedossaOppiaine' as const

export const isEiTiedossaOppiaine = (a: any): a is EiTiedossaOppiaine =>
  a?.$class === 'fi.oph.koski.schema.EiTiedossaOppiaine'
