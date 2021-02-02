import bem from "bem-ts"
import React from "react"
import "./Icon.less"

const b = bem("icon")

export type IconProps = {
  inline?: boolean
  color?: IconColor
}

export type IconColor = "warning" | "error"

const defineIcon = (name: string, color?: IconColor) => (props: IconProps) => (
  <MaterialDesignIcon name={name} color={color} {...props} />
)

export const SearchIcon = defineIcon("search")
export const ArrowDropDownIcon = defineIcon("arrow_drop_down")
export const ArrowDropUpIcon = defineIcon("arrow_drop_up")
export const CloseIcon = defineIcon("close")
export const WarningIcon = defineIcon("warning", "warning")
export const BackIcon = defineIcon("arrow_back")

type MaterialDesignIconProps = IconProps & {
  name: string // Ikonien nimet löytyvät osoitteesta https://material.io/resources/icons/?style=baseline
}

const MaterialDesignIcon = (props: MaterialDesignIconProps) => (
  <i
    className={`material-icons ${b({
      inline: props.inline,
      warning: props.color === "warning",
      error: props.color === "error",
    })}`}
  >
    {props.name}
  </i>
)
