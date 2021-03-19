import React from "react"
import { OpenInNewIcon } from "../icons/Icon"

export type ExternalLinkProps = {
  to: string
  children: React.ReactNode
}

export const ExternalLink = (props: ExternalLinkProps) => (
  <a href={props.to} target="_blank" className="externallink">
    {props.children} <OpenInNewIcon inline />
  </a>
)
