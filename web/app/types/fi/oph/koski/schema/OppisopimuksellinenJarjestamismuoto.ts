import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Oppisopimus } from './Oppisopimus'

/**
 * Koulutuksen järjestäminen oppisopimuskoulutuksena. Sisältää oppisopimuksen lisätiedot
 *
 * @see `fi.oph.koski.schema.OppisopimuksellinenJärjestämismuoto`
 */
export type OppisopimuksellinenJärjestämismuoto = {
  $class: 'fi.oph.koski.schema.OppisopimuksellinenJärjestämismuoto'
  tunniste: Koodistokoodiviite<'jarjestamismuoto', '20'>
  oppisopimus: Oppisopimus
}

export const OppisopimuksellinenJärjestämismuoto = (o: {
  tunniste?: Koodistokoodiviite<'jarjestamismuoto', '20'>
  oppisopimus: Oppisopimus
}): OppisopimuksellinenJärjestämismuoto => ({
  $class: 'fi.oph.koski.schema.OppisopimuksellinenJärjestämismuoto',
  tunniste: Koodistokoodiviite({
    koodiarvo: '20',
    koodistoUri: 'jarjestamismuoto'
  }),
  ...o
})

export const isOppisopimuksellinenJärjestämismuoto = (
  a: any
): a is OppisopimuksellinenJärjestämismuoto =>
  a?.$class === 'OppisopimuksellinenJärjestämismuoto'
