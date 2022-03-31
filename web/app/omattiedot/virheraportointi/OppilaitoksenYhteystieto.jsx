import React from 'react'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'

const emailSubject = t('Tiedustelu opintopolun tiedoista')
const mailTo = (to, message) => `mailto:${to}?subject=${encodeURIComponent(emailSubject)}&body=${encodeURIComponent(message)}`

export const OppilaitoksenYhteystieto = ({yhteystieto, message}) => (
  <div className='yhteystieto'>
    <div className='yhteystieto__heading'>
      <Text name='Voit tiedustella asiaasi lähettämällä sähköpostia osoitteeseen:'/>
    </div>
    <div className='yhteystieto__contact-info'>
      <a href={mailTo(yhteystieto.email, message)} className='yhteytieto__email'>{yhteystieto.email}</a>
      <span className='yhteytieto__name'>{t(yhteystieto.organisaationNimi)}</span>
    </div>
    <div className='yhteystieto__linkki'>
      <a href={mailTo(yhteystieto.email, message)} className='koski-button'>
        <Text name='Avaa sähköpostissa' className='yhteytieto__email'/>
      </a>
    </div>
  </div>
)

OppilaitoksenYhteystieto.displayName = 'OppilaitoksenYhteystieto'
