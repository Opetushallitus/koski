import React, { useMemo } from 'react'
import {
  KoodistokoodiviiteKoodistonNimellä,
  useKoodisto
} from '../../appstate/koodisto'
import { groupKoodistoToOptions } from '../../components-v2/controls/Select'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogField } from '../state/state'
import { DialogSelect } from './DialogSelect'

export type DialogKoodistoSelectProps<U extends string> = {
  state: DialogField<Koodistokoodiviite<U>>
  default?: string
  koodistoUri: U
  koodiarvot?: string[]
  formatLabel?: (koodi: KoodistokoodiviiteKoodistonNimellä) => string
  testId: string
}

export const DialogKoodistoSelect = <U extends string>(
  props: DialogKoodistoSelectProps<U>
) => {
  const koodisto = useKoodisto<U>(props.koodistoUri, props.koodiarvot)
  const options = useMemo(
    () =>
      koodisto
        ? groupKoodistoToOptions(koodisto, undefined, props.formatLabel)
        : [],
    [koodisto, props.formatLabel]
  )

  return (
    <DialogSelect
      options={options}
      initialValue={props.default && `${props.koodistoUri}_${props.default}`}
      value={props.state.value && koodistokoodiviiteId(props.state.value)}
      onChange={(opt) => props.state.set(opt?.value)}
      testId={props.testId}
    />
  )
}
