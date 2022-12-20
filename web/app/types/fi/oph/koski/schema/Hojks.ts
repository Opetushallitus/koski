import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Jos kyseessä erityisopiskelija, jolle on tehty henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (HOJKS), täytetään tämä tieto. Käytetään vain mikäli HOJKS-päätös on tehty ennen vuotta 2018. 2018 lähtien tieto välitetään erityinenTuki-rakenteen kautta. Rahoituksen laskennassa hyödynnettävä tieto.
 *
 * @see `fi.oph.koski.schema.Hojks`
 */
export type Hojks = {
  $class: 'fi.oph.koski.schema.Hojks'
  opetusryhmä: Koodistokoodiviite<'opetusryhma', string>
  alku?: string
  loppu?: string
}

export const Hojks = (o: {
  opetusryhmä: Koodistokoodiviite<'opetusryhma', string>
  alku?: string
  loppu?: string
}): Hojks => ({ $class: 'fi.oph.koski.schema.Hojks', ...o })

export const isHojks = (a: any): a is Hojks =>
  a?.$class === 'fi.oph.koski.schema.Hojks'
