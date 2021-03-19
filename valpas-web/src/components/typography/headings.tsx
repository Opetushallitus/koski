import bem from "bem-ts"
import React from "react"
import { plainComponent } from "../../utils/plaincomponent"
import "./headings.less"

export type HeadingProps = {
  children: React.ReactNode
}

const b = bem("heading")

export const Heading = plainComponent("h1", b(["primary"]))
export const SecondaryHeading = plainComponent("h2", b(["secondary"]))
export const TertiaryHeading = plainComponent("h3", b(["tertiary"]))
