import React from 'react'
import { t } from '../../i18n/i18n'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { CHARCODE_REMOVE, IconButton } from './IconButton'

export type RemovableProps = CommonPropsWithChildren<{
  onClick: () => void
}>

export const Removable: React.FC<RemovableProps> = (props) => (
  <div {...common(props, ['Removable'])}>
    <div className="Removable__content">{props.children}</div>
    <IconButton
      charCode={CHARCODE_REMOVE}
      label={t('poista')}
      size="input"
      onClick={props.onClick}
    />
  </div>
)
