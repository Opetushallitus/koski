import React, { useMemo } from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'

export type IconProps = CommonProps<{
  charCode: string
}>

export const Icon: React.FC<IconProps> = (props) => {
  const icon = useMemo(
    () => String.fromCharCode(parseInt(props.charCode, 16)),
    [props.charCode]
  )

  return <span {...common(props, ['Icon'])}>{icon}</span>
}

export type IconLabelProps = CommonProps<{
  charCode: string
  children: string | LocalizedString
}>

export const IconLabel: React.FC<IconLabelProps> = (props) => (
  <span {...common(props, ['IconLabel'])}>
    <Icon charCode={props.charCode} /> {t(props.children)}
  </span>
)

// Lisää ikonikoodeja löytyy osoitteesta https://fontawesome.com/icons

export const CHARCODE_ADD = 'f055'
export const CHARCODE_REMOVE = 'f1f8'
export const CHARCODE_OPEN = 'f0fe'
export const CHARCODE_CLOSE = 'f146'
