import bem from "bem-ts"
import React from "react"
import { plainComponent } from "../../utils/plaincomponent"
import "./cards.less"

const b = bem("card")

export type CardProps = React.HTMLAttributes<HTMLDivElement>

const CardContainer = plainComponent("section", b())
const CardBorders = plainComponent("div", b("borders"))

export const Card = ({ children, ...props }: CardProps) => (
  <CardContainer {...props}>
    <CardBorders>{children}</CardBorders>
  </CardContainer>
)

export const BorderlessCard = plainComponent("section", b(["borderless"]))

export const CardHeader = plainComponent("header", b("header"))

export const CardBody = plainComponent("div", b("body"))
