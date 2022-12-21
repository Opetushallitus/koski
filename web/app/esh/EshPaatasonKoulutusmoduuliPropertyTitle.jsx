import React from 'baret'
import { Infobox } from '../components/Infobox'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import ExternalLinkIcon from '../omadata/luvanhallinta/ExternalLinkIcon'

export const EshPäätasonKoulutusmoduuliPropertyTitle = ({ title }) => (
  <>
    <Text name={title} />
    <Infobox>
      <Text
        className="bold"
        name="Helsingin eurooppalaisen koulun opetussuunnitelmat"
      />
      <br />
      <Text name="Helsingin eurooppalaisen koulun opetussuunnitelmista ja arvosana-asteikoista on lisätietoja" />{' '}
      <a
        href={t('infoLinkUrl:Eurooppakoulujen opetussuunnitelmat') || '#'}
        target={'_blank'}
        rel="noopener noreferrer"
      >
        {t('Eurooppakoulujen sivulla')} <ExternalLinkIcon />
      </a>
    </Infobox>
  </>
)
