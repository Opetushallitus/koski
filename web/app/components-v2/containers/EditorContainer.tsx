import React from 'react'
import { baseProps, BaseProps } from '../baseProps'

export type EditorContainerProps = BaseProps & {
  children?: React.ReactNode
}

export const EditorContainer = (props: EditorContainerProps) => (
  <div {...baseProps(props, 'EditorContainer')}>{props.children}</div>
)
