import bem from "bem-ts"
import React from "react"
import { NavLink, Route } from "react-router-dom"
import { useBasePath } from "../../state/basePath"
import { Caret } from "../icons/Caret"
import "./TabNavigation.less"

const b = bem("tabnavigation")

export type TabNavigationProps = {
  options: TabNavigationItem[]
}

export type TabNavigationItem = {
  display: React.ReactNode
  linkTo: string
}

export const TabNavigation = (props: TabNavigationProps) => {
  const basePath = useBasePath()
  return (
    <nav className={b()}>
      <ul className={b("list")}>
        {props.options.map((option, index) => (
          <TabNavigationLink key={index} to={`${basePath}${option.linkTo}`}>
            {option.display}
          </TabNavigationLink>
        ))}
      </ul>
    </nav>
  )
}

type TabNavigationLinkProps = {
  to: string
  children: React.ReactNode
}

const TabNavigationLink = (props: TabNavigationLinkProps) => (
  <li className={b("itemcontainer")}>
    <NavLink
      exact
      to={props.to}
      className={b("item")}
      activeClassName={b("item", ["selected"])}
    >
      {props.children}
    </NavLink>
    <div className={b("caretwrapper")}>
      <Route exact path={props.to}>
        <Caret width={24} direction="down" />
      </Route>
    </div>
  </li>
)
