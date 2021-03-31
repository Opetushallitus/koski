import bem from "bem-ts"
import React from "react"
import { plainComponent } from "../../utils/plaincomponent"
import "./IconSection.less"

const b = bem("iconsection")

export type IconSectionProps = React.HTMLAttributes<HTMLElement> & {
  icon: React.ReactNode
  children: React.ReactNode
}

export const IconSection = ({ icon, children, ...rest }: IconSectionProps) => (
  <section {...rest} className={b()}>
    <div className={b("icon")}>{icon}</div>
    <div className={b("content")}>{children}</div>
  </section>
)

export type IconSectionHeadingProps = {
  children: React.ReactNode
}

export const IconSectionHeading = plainComponent("h3", b("heading"))
