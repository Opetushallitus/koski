import bem from "bem-ts"
import React from "react"

const b = bem("caret")

export type CaretDirection = "down" | "up"

export type CaretProps = {
  width: number
  height?: number
  direction: CaretDirection
}

export const Caret = (props: CaretProps) => {
  const w = props.width
  const h = props.height || w * 0.7
  const path =
    props.direction === "down"
      ? `M 0 0 L ${w / 2} ${h} L ${w} 0`
      : `M 0 ${h} L ${w / 2} 0 L ${w} ${h}`

  return (
    <svg
      width={w}
      height={h}
      viewBox={`0 0 ${w} ${h}`}
      xmlns="http://www.w3.org/2000/svg"
      className={b([props.direction])}
    >
      <path className={b("fill")} d={`${path} Z`} />
      <path className={b("stroke")} d={path} />
    </svg>
  )
}
