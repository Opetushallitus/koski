import React from 'react'
import Text from '../i18n/Text'

const descriptions = ['aria-description:date-input']

const descriptionElm = (id) => (
  <div hidden={true} aria-hidden={true} id={id}>
    <Text name={id} />
  </div>
)

const ariaDescription = descriptions.reduce(
  (obj, cur) => ({ ...obj, [cur]: descriptionElm(cur) }),
  {}
)

export const HiddenDescription = ({ id }) => {
  const elm = ariaDescription[id]
  if (!elm) throw new Error(`No description declared for ${id}`)
  return elm
}
