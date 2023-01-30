import React from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'

export type EditorContainerProps = CommonPropsWithChildren

export const EditorContainer = (props: EditorContainerProps) => (
  <div {...common(props, ['EditorContainer'])}>{props.children}</div>
)
