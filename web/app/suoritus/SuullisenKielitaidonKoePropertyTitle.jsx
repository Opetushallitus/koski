import React from 'react'
import { Infobox } from '../components/Infobox'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import { LocalizedExternalLink } from '../i18n/LocalizedExternalLink'

const links = {
  fi: 'https://www.oph.fi/fi/koulutus-ja-tutkinnot/kehittyvan-kielitaidon-tasojen-kuvausasteikko',
  sv: 'https://www.oph.fi/sv/utbildning-och-examina/nivaskalan-sprakkunskap-och-sprakutveckling'
}

export const SuullisenKielitaidonKoePropertyTitle = () => (
  <>
    <Text name={'Suullisen kielitaidon kokeet'} />
    <Infobox>
      <Text className="bold" name="Suullisen kielitaidon koe" />
      <br />
      <Text name="Lukion toisen kotimaisen ja vieraiden kielten tiettyihin oppimääriin liittyvä suullisen kielitaidon kurssi, jonka suorittamiseen kuuluu Opetushallituksen tuottama suullisen kielitaidon koe." />{' '}
      <Text name="Suullisen kielitaidon kokeen arvioinnissa käytetään" />{' '}
      <LocalizedExternalLink options={links}>
        {t('kehittyvän kielitaidon tasojen kuvausasteikkoa')}
      </LocalizedExternalLink>
    </Infobox>
  </>
)
