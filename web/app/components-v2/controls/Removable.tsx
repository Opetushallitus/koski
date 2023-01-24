import React from 'react'
import { t } from '../../i18n/i18n'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { CHARCODE_REMOVE } from '../texts/Icon'
import { IconButton } from './IconButton'

export type RemovableProps = CommonPropsWithChildren<{
  isRemovable?: boolean // default true
  onClick: () => void
}>

export const Removable: React.FC<RemovableProps> = (props) =>
  props.isRemovable === false ? (
    <div {...common(props, ['NotRemovable'])}>
      <div className="NotRemovable__content">{props.children}</div>
    </div>
  ) : (
    <div {...common(props, ['Removable'])}>
      <div className="Removable__content">{props.children}</div>
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        size="input"
        onClick={props.onClick}
      />
    </div>
  )
