import bem from "bem-ts"
import React from "react"
import { OpenInNewIcon } from "../icons/Icon"
import "./ExternalLink.less"

const b = bem("externallink")

export type ExternalLinkProps = {
  to: string
  children: React.ReactNode
}

export const ExternalLink = (props: ExternalLinkProps) => (
  <a href={props.to} target="_blank" className="externallink">
    {props.children} <OpenInNewIcon inline className={b("icon")} />
  </a>
)
