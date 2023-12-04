import { isString } from 'fp-ts/string'
import React from 'react'
import { filterObjByKey } from '../util/fp/objects'

export type CommonProps<T extends object = object> = T & {
  className?: string
  style?: React.CSSProperties
  role?: string
}

export type CommonPropsWithChildren<T extends object = object> =
  React.PropsWithChildren<CommonProps<T>>

export type MaybeClassName = string | undefined | null | false | 0

export const cx = (...args: MaybeClassName[]): string =>
  args.filter(isString).join(' ')

export const common = <T extends object>(
  props: CommonProps<T>,
  classNames: MaybeClassName[] = []
): object => ({
  style: props.style,
  className: cx(props.className, ...classNames),
  role: props.role,
  ...filterObjByKey(startsWith('aria-', 'data-'))(props)
})

export const rest = <T extends object>({
  style,
  className,
  role,
  ...restOfProps
}: CommonProps<T>) => doesNotStartWith('aria-', 'data-')(restOfProps)

export const startsWith =
  (...searchStrings: string[]) =>
  (input: any): boolean =>
    typeof input === 'string' && searchStrings.some((s) => input.startsWith(s))

export const doesNotStartWith =
  (...searchStrings: string[]) =>
  (input: any): boolean =>
    !startsWith(...searchStrings)(input)
