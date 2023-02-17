import React, { useMemo } from 'react'
import { t, tExists } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { capitalize, uncapitalize } from '../../util/strings'

export type TransProps = {
  children?: LocalizedString | string
}

export const Trans = (props: TransProps) => {
  const text = useMemo(() => {
    const key = props.children
    if (typeof key === 'string') {
      if (tExists(key)) {
        return t(key)
      }
      const lowerKey = uncapitalize(key)
      if (tExists(lowerKey)) {
        return capitalize(t(lowerKey))
      }
      const upperKey = capitalize(key)
      if (tExists(upperKey)) {
        return uncapitalize(t(upperKey))
      }
    }
    return t(key)
  }, [props.children])

  return <>{text || null}</>
}
