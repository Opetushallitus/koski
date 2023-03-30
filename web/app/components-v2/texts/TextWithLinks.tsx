import React, { useMemo } from 'react'
import { nonNull } from '../../util/fp/arrays'
import { common, CommonProps } from '../CommonProps'

export type TextWithLinksProps = CommonProps<{
  children: string
}>

export const TextWithLinks: React.FC<TextWithLinksProps> = (props) => {
  const nodes = useMemo(() => split(props.children), [props.children])
  return <span {...common(props)}>{...nodes}</span>
}

type Matcher = (str: string) => [string, React.ReactNode, string] | null

const simpleEmail: Matcher = (str) => {
  const match = str.match(/(.*?)([\w\d.]+@[\w\d.]+[\w\d]+)(.*)/)
  return match
    ? [match[1], <a href={`mailto:${match[2]}`}>{match[2]}</a>, match[3]]
    : null
}

const simpleUrl: Matcher = (str) => {
  const match = str.match(/(.*?)([\w]+:\/\/[\w\d./]+)(.*)/)
  return match
    ? [
        match[1],
        <a href={match[2]} target="_blank" rel="noreferrer">
          {match[2]}
        </a>,
        match[3]
      ]
    : null
}

const matchNext = (str: string): [React.ReactNode[], string] => {
  const results = [simpleEmail, simpleUrl].map((f) => f(str)).filter(nonNull)
  const nearest = results.reduce(
    (acc, r) => (r[0].length < acc[0].length ? r : acc),
    results[0]
  )
  if (nearest !== null) {
    const [prependingText, node, textLeft] = nearest
    const nodes =
      prependingText === '' ? [node] : [<span>{prependingText}</span>, node]
    return [nodes, textLeft]
  }
  return [[<span>{str}</span>], '']
}

const split = (input: string): React.ReactNode[] => {
  const result: React.ReactNode[] = []
  while (input.length > 0) {
    const [nodes, next] = matchNext(input)
    result.push(...nodes)
    input = next
  }
  return result
}
