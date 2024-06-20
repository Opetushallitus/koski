import React, { useMemo } from 'react'
import { usePerusteSelectOptions } from '../../appstate/peruste'
import { Select } from '../../components-v2/controls/Select'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { t } from '../../i18n/i18n'

export type DialogPerusteSelectProps = {
  state: UusiOpiskeluoikeusDialogState
  default?: string
  filter?: (diaarinumero: string) => boolean
}

export const DialogPerusteSelect = <U extends string>(
  props: DialogPerusteSelectProps
) => {
  const options = usePerusteSelectOptions(
    props.state.päätasonSuoritus.value?.koodiarvo
  )
  const filtered = useMemo(() => {
    const filter = props.filter
    return filter
      ? options.filter((opt) => !opt.value || filter(opt.value.koodiarvo))
      : options
  }, [options, props.filter])

  return (
    <>
      {t('Peruste')}
      <Select
        options={filtered}
        initialValue={props.default || options[0]?.value?.koodiarvo}
        value={props.state.peruste.value?.koodiarvo}
        onChange={(opt) => props.state.peruste.set(opt?.value)}
        disabled={options.length < 2}
        testId="peruste"
      />
    </>
  )
}
