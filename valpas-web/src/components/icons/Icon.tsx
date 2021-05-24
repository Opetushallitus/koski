import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./Icon.less"

const b = bem("icon")

export type IconProps = {
  inline?: boolean
  color?: IconColor
  className?: string
}

export type IconColor =
  | "warning"
  | "error"
  | "gray"
  | "blue"
  | "success"
  | "white"
  | "green"

const defineIcon = (name: string, color?: IconColor) => (props: IconProps) => (
  <MaterialDesignIcon name={name} color={color} {...props} />
)

export const SearchIcon = defineIcon("search")
export const ArrowDropDownIcon = defineIcon("arrow_drop_down")
export const ArrowDropUpIcon = defineIcon("arrow_drop_up")
export const CloseIcon = defineIcon("close")
export const WarningIcon = defineIcon("warning", "warning")
export const BackIcon = defineIcon("arrow_back")
export const HakuIcon = defineIcon("list_alt")
export const OpiskeluIcon = defineIcon("school")
export const InfoIcon = defineIcon("info_outline", "blue")
export const CaretDownIcon = defineIcon("keyboard_arrow_down")
export const CaretUpIcon = defineIcon("keyboard_arrow_up")
export const CaretRightIcon = defineIcon("keyboard_arrow_right")
export const CaretLeftIcon = defineIcon("keyboard_arrow_left")
export const OpenInNewIcon = defineIcon("open_in_new")
export const SuccessIcon = defineIcon("done", "success")
export const SuccessCircleIcon = defineIcon("check_circle", "success")
export const FutureSuccessIcon = defineIcon("hourglass_empty", "success")
export const IlmoitusIcon = defineIcon("error_outline")
export const IlmoitusListIcon = defineIcon("schedule")

type MaterialDesignIconProps = IconProps & {
  name: string // Ikonien nimet löytyvät osoitteesta https://material.io/resources/icons/?style=baseline
}

const MaterialDesignIcon = (props: MaterialDesignIconProps) => (
  <span
    aria-hidden
    className={joinClassNames(
      "material-icons",
      b({
        inline: props.inline,
        ...(props.color && {
          [props.color]: true,
        }),
      }),
      props.className
    )}
  >
    {props.name}
  </span>
)
