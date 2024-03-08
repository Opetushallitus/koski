import React, { useMemo } from 'react'
import { t, tExists } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { capitalize, uncapitalize } from '../../util/strings'

export type TransProps = {
  children?: LocalizedString | string
}

export const Trans = (props: TransProps) => {
  const text = useMemo(() => {
    return t(props.children)
  }, [props.children])

  return <>{text || null}</>
}
