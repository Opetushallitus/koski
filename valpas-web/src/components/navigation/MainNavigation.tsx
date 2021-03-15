import bem from "bem-ts"
import React from "react"
import { Caret } from "../icons/Caret"
import "./MainNavigation.less"

const b = bem("mainnavigation")

export type MainNavigationProps = {
  selected: string
  options: NavigationItem[]
  onChange: (key: string) => void
}

export type NavigationItem = {
  key: string
  display: React.ReactNode
}

export const MainNavigation = (props: MainNavigationProps) => (
  <nav className={b()}>
    <ul className={b("list")}>
      {props.options.map((option) => (
        <li
          key={option.key}
          className={b("item", {
            selected: props.selected === option.key,
          })}
          onClick={() => props.onChange(option.key)}
        >
          <span className={b("display")}>{option.display}</span>
          {props.selected === option.key && (
            <div className={b("caretwrapper")}>
              <Caret width={24} direction="down" />
            </div>
          )}
        </li>
      ))}
    </ul>
  </nav>
)
