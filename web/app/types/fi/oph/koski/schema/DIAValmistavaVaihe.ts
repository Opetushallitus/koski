import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Valmistavan DIA-vaiheen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAValmistavaVaihe`
 */
export type DIAValmistavaVaihe = {
  $class: 'fi.oph.koski.schema.DIAValmistavaVaihe'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
}

export const DIAValmistavaVaihe = (
  o: {
    tunniste?: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
  } = {}
): DIAValmistavaVaihe => ({
  $class: 'fi.oph.koski.schema.DIAValmistavaVaihe',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'diavalmistavavaihe',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isDIAValmistavaVaihe = (a: any): a is DIAValmistavaVaihe =>
  a?.$class === 'fi.oph.koski.schema.DIAValmistavaVaihe'
