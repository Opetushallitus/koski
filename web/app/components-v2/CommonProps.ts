import { isString } from 'fp-ts/string'
import React from 'react'

export type CommonProps<T extends object = {}> = T & {
  className?: string
  style?: React.CSSProperties
  testId?: string
}

export type CommonPropsWithChildren<T extends object = {}> =
  React.PropsWithChildren<CommonProps<T>>

export type MaybeClassName = string | undefined | null | false | 0

export const cx = (...args: MaybeClassName[]) => args.filter(isString).join(' ')

export const common = <T extends object>(
  props: CommonProps<T>,
  classNames: MaybeClassName[] = []
) => ({
  'data-testid': props.testId,
  style: props.style,
  className: cx(props.className, ...classNames)
})
