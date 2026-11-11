import React from 'react'
import { t } from '../i18n/i18n'

export const Teksti: React.FC<{ avain: string }> = ({ avain }) => (
  <span aria-label={t(avain)} className="localized">
    {t(avain)}
  </span>
)

export const Kappaleet: React.FC<{ avain: string }> = ({ avain }) => (
  <>
    {t(avain)
      .split('\n')
      .map((teksti, index) => (
        <p key={index}>{teksti}</p>
      ))}
  </>
)
