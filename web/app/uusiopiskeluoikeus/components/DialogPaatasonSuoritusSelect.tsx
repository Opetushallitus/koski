import React, { useMemo } from 'react'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { usePäätasonSuoritustyypit } from '../state/hooks'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { DialogSelect } from './DialogSelect'

export type DialogPäätasonSuoritusSelectProps = {
  state: UusiOpiskeluoikeusDialogState
  hiddenOptions?: string[]
  default?: string
  testId: string
}

export const DialogPäätasonSuoritusSelect = (
  props: DialogPäätasonSuoritusSelectProps
) => {
  const options = usePäätasonSuoritustyypit(props.state)
  const filtered = useMemo(
    () =>
      props.hiddenOptions
        ? options.filter(
            (opt) =>
              opt.value?.koodiarvo &&
              props.hiddenOptions?.includes(opt.value?.koodiarvo)
          )
        : options,
    [options, props.hiddenOptions]
  )

  return (
    <DialogSelect
      options={filtered}
      initialValue={props.default && `suorituksentyyppi_${props.default}`}
      value={
        props.state.päätasonSuoritus.value &&
        koodistokoodiviiteId(props.state.päätasonSuoritus.value)
      }
      onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
      testId={props.testId}
    />
  )
}
