import { LaajuusOpintopisteissäTaiKursseissa } from './LaajuusOpintopisteissaTaiKursseissa'

/**
 * DIAVastaavuustodistuksenTiedot
 *
 * @see `fi.oph.koski.schema.DIAVastaavuustodistuksenTiedot`
 */
export type DIAVastaavuustodistuksenTiedot = {
  $class: 'fi.oph.koski.schema.DIAVastaavuustodistuksenTiedot'
  keskiarvo: number
  lukioOpintojenLaajuus: LaajuusOpintopisteissäTaiKursseissa
}

export const DIAVastaavuustodistuksenTiedot = (o: {
  keskiarvo: number
  lukioOpintojenLaajuus: LaajuusOpintopisteissäTaiKursseissa
}): DIAVastaavuustodistuksenTiedot => ({
  $class: 'fi.oph.koski.schema.DIAVastaavuustodistuksenTiedot',
  ...o
})

export const isDIAVastaavuustodistuksenTiedot = (
  a: any
): a is DIAVastaavuustodistuksenTiedot =>
  a?.$class === 'DIAVastaavuustodistuksenTiedot'
