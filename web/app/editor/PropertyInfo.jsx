import React from 'baret'
import { Infobox } from '../components/Infobox'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'

export const PropertyInfo = ({property}) => {
  if(!(property.model && (property.model.infoDescription || (property.model.infoLinkTitle && property.model.infoLinkUrl)))) {
    return null
  }
  return  (
  <Infobox>
    {property.model.infoDescription && <><Text name={`infoDescription:${property.model.infoDescription}`} data-test-id={`${property?.key || 'unknown-property'}-info-box`} className={`${property?.key || 'unknown-property'}-info-box`}/><br/></>}
    {property.model.infoLinkTitle && property.model.infoLinkUrl && <a href={t(`infoLinkUrl:${property.model.infoLinkUrl}`)} target='_blank' rel='noopener noreferrer' data-test-id={`${property?.key || 'unknown-property'}-info-link`} className={`${property?.key || 'unknown-property'}-info-link`}>{t(`infoLinkTitle:${property.model.infoLinkTitle}`)}</a>}
  </Infobox>)
}
