import React from 'baret'
import Text from '../../i18n/Text'
import { t } from '../../i18n/i18n'
import ExternalLinkIcon from './ExternalLinkIcon'

export const LuvanHallintaDisclaimer = () => (
  <div className="kayttoluvat-disclaimer">
    <p>
      {'*'}
      <Text name="Koskee vain opintotietoja --prefix" />
      <a
        className="kayttoluvat-external-link"
        href={t('tietosuojaseloste-link')}
        target="_blank"
        rel="noopener noreferrer"
      >
        <ExternalLinkIcon />
        <Text name="Koskee vain opintotietoja --link-title" />
      </a>
      <Text name="Koskee vain opintotietoja --suffix" />
    </p>
  </div>
)
