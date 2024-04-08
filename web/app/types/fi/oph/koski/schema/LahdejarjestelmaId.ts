import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. Käytetään silloin, kun opiskeluoikeus on tuotu Koskeen tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä opintohallintojärjestelmästä.
 *
 * @see `fi.oph.koski.schema.LähdejärjestelmäId`
 */
export type LähdejärjestelmäId = {
  $class: 'fi.oph.koski.schema.LähdejärjestelmäId'
  id?: string
  lähdejärjestelmä: Koodistokoodiviite<'lahdejarjestelma', string>
}

export const LähdejärjestelmäId = (o: {
  id?: string
  lähdejärjestelmä: Koodistokoodiviite<'lahdejarjestelma', string>
}): LähdejärjestelmäId => ({
  $class: 'fi.oph.koski.schema.LähdejärjestelmäId',
  ...o
})

LähdejärjestelmäId.className = 'fi.oph.koski.schema.LähdejärjestelmäId' as const

export const isLähdejärjestelmäId = (a: any): a is LähdejärjestelmäId =>
  a?.$class === 'fi.oph.koski.schema.LähdejärjestelmäId'
