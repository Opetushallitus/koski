import React from 'react'
import { Select, SelectProps } from '../../components-v2/controls/Select'

export const DialogSelect = <T,>(props: SelectProps<T>) => (
  <Select autoselect inlineOptions {...props} />
)
