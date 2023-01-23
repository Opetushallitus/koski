import React from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonPropsWithChildren } from '../CommonProps'

type NewType = CommonPropsWithChildren<{
  label: string | LocalizedString
}>

export type LabelProps = NewType

export const Label: React.FC<LabelProps> = (props) => {
  return (
    <label {...common(props, ['Label'])}>
      <div className="Label__label">{t(props.label)}</div>
      <div className="Label__field">{props.children}</div>
    </label>
  )
}
