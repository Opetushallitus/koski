// Version EnumValuesta, jossa kulkee mukana tyyppitieto

import { KoodistokoodiviiteKoodistonNimellä } from '../appstate/koodisto'
import {
  groupKoodistoToOptions,
  SelectOption
} from '../components-v2/controls/Select'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodistokoodiviiteId } from './koodisto'

export type TypedEnumValue<T> = {
  $class: 'fi.oph.koski.editor.EnumValue'
  value: string
  title: string
  data: T
  groupName?: string
}

export const enumValuesToKoodistoSelectOptions = <T extends Koodistokoodiviite>(
  ts: TypedEnumValue<T>[]
): SelectOption<T>[] => {
  const koodit: KoodistokoodiviiteKoodistonNimellä[] = ts.map((t) => ({
    koodiviite: t.data,
    id: koodistokoodiviiteId(t.data),
    koodistoNimi: t.groupName || t.data.koodistoUri
  }))
  return groupKoodistoToOptions(koodit) as SelectOption<T>[]
}
