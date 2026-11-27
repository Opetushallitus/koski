import React from 'react'
import Link from '../components/Link'
import Text from '../i18n/Text'

export const naviLink = (
  path,
  textKey,
  location,
  linkClassName,
  isSelected = (p, l) => p === l
) => {
  let className =
    textKey.toLowerCase().replace(/ /g, '') + ' navi-link-container'
  className = isSelected(path, location) ? className + ' selected' : className
  return (
    <span className={className}>
      <Link href={path} className={linkClassName}>
        <Text name={textKey} />
      </Link>
    </span>
  )
}
