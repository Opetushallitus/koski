import bem from "bem-ts"
import React from "react"
import { NavLink } from "react-router-dom"
import { useBasePath } from "../../state/basePath"
import "./MainNavigation.less"

const b = bem("mainnavigation")

export type MainNavigationProps = {
  title: string
  options: MainNavigationItem[]
}

export type MainNavigationItem = {
  display: React.ReactNode
  linkTo: string
}

export const MainNavigation = (props: MainNavigationProps) => {
  const basePath = useBasePath()
  return (
    <nav className={b()}>
      <div className={b("title")}>{props.title}:</div>
      {props.options.map((option, index) => (
        <MainNavigationLink key={index} to={`${basePath}${option.linkTo}`}>
          {option.display}
        </MainNavigationLink>
      ))}
    </nav>
  )
}

type MainNavigationLinkProps = {
  to: string
  children: React.ReactNode
}

const MainNavigationLink = (props: MainNavigationLinkProps) => (
  <NavLink
    to={props.to}
    className={b("item")}
    activeClassName={b("item", ["selected"])}
  >
    {props.children}
  </NavLink>
)
