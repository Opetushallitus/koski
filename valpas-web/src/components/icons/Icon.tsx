import bem from "bem-ts"
import React from "react"
import "./Icon.less"

const b = bem("icon")

export type IconProps = {
  inline?: boolean
}

const defineIcon = (name: string) => (props: IconProps) => (
  <MaterialDesignIcon name={name} {...props} />
)

export const SearchIcon = defineIcon("search")
export const ArrowDropDownIcon = defineIcon("arrow_drop_down")
export const ArrowDropUpIcon = defineIcon("arrow_drop_up")

type MaterialDesignIconProps = IconProps & {
  name: string // Ikonien nimet löytyvät osoitteesta https://material.io/resources/icons/?style=baseline
}

const MaterialDesignIcon = (props: MaterialDesignIconProps) => (
  <i className={`material-icons ${b({ inline: props.inline })}`}>
    {props.name}
  </i>
)
