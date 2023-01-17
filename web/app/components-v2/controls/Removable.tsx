import React from 'react'
import { t } from '../../i18n/i18n'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { FlatButton } from './FlatButton'
import { IconButton } from './IconButton'

export type RemovableProps = CommonPropsWithChildren<{
  onClick: () => void
}>

export const Removable: React.FC<RemovableProps> = (props) => (
  <div {...common(props, ['Removable'])}>
    <div className="Removable__content">{props.children}</div>
    <IconButton
      charCode="f1f8"
      label={t('poista')}
      size="input"
      onClick={props.onClick}
    />
  </div>
)
