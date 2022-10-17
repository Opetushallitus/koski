import React from 'baret'
import Atom from 'bacon.atom'
import { ift } from '../util/util'
import { t } from '../i18n/i18n'
import * as R from 'ramda'

export const Infobox = ({ children }) => {
  const showInfo = Atom(false)

  return (
    <span className="infobox">
      <span className="info-icon" onClick={() => showInfo.set(true)} />
      {ift(
        showInfo.map(R.identity),
        <InfoText onClick={() => showInfo.set(false)}>{children}</InfoText>
      )}
    </span>
  )
}

const InfoText = ({ onClick, children }) => (
  <>
    <div className="info-content-shield" onClick={onClick} />
    <span className="info-content">
      <button
        className="info-content-close"
        onClick={onClick}
        aria-label={t('Sulje')}
      />
      {children}
    </span>
  </>
)
