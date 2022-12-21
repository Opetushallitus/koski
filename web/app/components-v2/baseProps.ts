import { isString } from 'fp-ts/string'
import { isObjectWithProp } from '../util/fp/objects'

export type BaseProps = {
  className?: string
  testId?: string
}

const isPropsWithClassName = isObjectWithProp('className', isString)
const isPropsWithTestId = isObjectWithProp('testId', isString)

export const baseProps = (
  ...args: Array<string | undefined | null | false | 0 | BaseProps>
) => {
  const classNames: string[] = [
    ...args.filter(isString),
    ...args.filter(isPropsWithClassName).map((p) => p.className)
  ]

  const testId = args.filter(isPropsWithTestId).map((p) => p.testId)[0]

  return {
    className: classNames.join(' '),
    'data-testid': testId
  }
}
