import React from 'react'
import { Trans } from '../components-v2/texts/Trans'

export type ErrorPageProps = {
  title?: string
  text?: string
}

const ErrorPage = ({
  title = 'Omadata virhe',
  text = 'Tapahtuman käsittelyssä tapahtui virhe'
}: ErrorPageProps) => (
  <div className="error-container">
    <div className="heading">
      <h1>
        <Trans>{title}</Trans>
      </h1>
    </div>
    <div className="error-text">
      <Trans>{text}</Trans>
    </div>
  </div>
)

export default ErrorPage
