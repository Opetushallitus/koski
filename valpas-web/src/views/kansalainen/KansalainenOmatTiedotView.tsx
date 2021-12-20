import React from "react"
import { Page } from "../../components/containers/Page"
import { Heading } from "../../components/typography/headings"
import { User } from "../../state/common"

export type KansalainenOmatTiedotViewProps = {
  user: User
}

export const KansalainenOmatTiedotView = (
  props: KansalainenOmatTiedotViewProps
) => (
  <Page>
    <Heading>Valpas</Heading>
    <p>Hei, {props.user.name}! Tähän tulee Valpas-järjestelmän oppijanäkymä.</p>
  </Page>
)
