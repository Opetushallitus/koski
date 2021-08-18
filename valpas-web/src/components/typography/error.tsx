import bem from "bem-ts"
import React from "react"
import { ApiError } from "../../api/apiFetch"
import { T } from "../../i18n/i18n"
import "./Error.less"

const b = bem("error")

export type ErrorProps = {
  children: React.ReactNode
}

export const Error = (props: ErrorProps) => (
  <div className={b()}>{props.children}</div>
)

export type ApiErrorsProps = {
  errors: ApiError[]
}

export const ApiErrors = (props: ApiErrorsProps) => (
  <Error>
    {props.errors.length > 0 ? (
      <ul>
        {props.errors.map((error, index) => (
          <li key={index}>{error.message}</li>
        ))}
      </ul>
    ) : (
      <T id="apivirhe__tuntematon" />
    )}
  </Error>
)
