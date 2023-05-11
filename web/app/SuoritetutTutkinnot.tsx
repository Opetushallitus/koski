import { isSuccess, useApiWithParams } from './api-fetch'
import { fetchSuoritetutTutkinnot } from './util/koskiApi'
import * as R from 'ramda'
import React from 'react'
import ReactDOM from 'react-dom'
import { isSuoritetutTutkinnotOppija } from './types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotOppija'
import { SuoritusjakoTopBar } from './components-v2/layout/SuoritusjakoTopBar'
import { t } from './i18n/i18n'
import { ContentContainer } from './components-v2/containers/ContentContainer'
import { PlainList } from './components-v2/containers/PlainList'
import { isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus } from './types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
import { Trans } from './components-v2/texts/Trans'
import { Flex } from './components-v2/containers/Flex'
// @ts-ignore
__webpack_nonce__ = window.nonce
// @ts-ignore
import(/* webpackChunkName: "styles" */ './style/main.less')

const secret = R.last(document.location.pathname.split('/')) ?? ''
interface SuoritetutTutkinnotProps {
  testId?: string
}
const SuoritetutTutkinnot = ({ testId }: SuoritetutTutkinnotProps) => {
  const response = useApiWithParams(fetchSuoritetutTutkinnot, [secret])
  return isSuccess(response) && isSuoritetutTutkinnotOppija(response.data) ? (
    <>
      <SuoritusjakoTopBar />
      <ContentContainer className="content-area suoritusjako" testId={testId}>
        <div className="main-content">
          <h1>
            <Trans>{'Suoritetut tutkinnot'}</Trans>
          </h1>
          <a
            className="text-button-small"
            target="_blank"
            href={`/koski/api/opinnot/suoritetut-tutkinnot/${secret}`}
            rel="noopener noreferrer"
          >
            <Trans>{'Tiedot koneluettavassa muodossa'}</Trans>
          </a>
          <div>
            <div className="header__name">
              <p className="textstyle-like-h2">
                {response.data.henkilö.etunimet}{' '}
                {response.data.henkilö.sukunimi}
              </p>
            </div>
          </div>

          <div></div>

          <div>
            <p>
              <Trans>
                {
                  'Tässä listataan suoritetut tutkinnot otsikkotasolla. Tarkemmat tiedot löytyvät koneluettavassa muodossa yllä olevasta linkistä.'
                }
              </Trans>
            </p>
            <PlainList>
              {response.data.opiskeluoikeudet.map((oo) => (
                <div key={oo.$class}>
                  <h3>{oo.oppilaitos?.nimi?.en}</h3>
                  <Flex>
                    <span>
                      <Trans>{oo.tyyppi.nimi}</Trans>
                      {': '}
                      {oo.suoritukset.map((s) => (
                        <Trans>{s.koulutusmoduuli.tunniste.nimi}</Trans>
                      ))}
                    </span>
                    <span>
                      {isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(oo)
                        ? `Opiskeluoikeuden oid: ${oo.oid}`
                        : ''}
                    </span>
                    <span>
                      {isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(oo)
                        ? `Vahvistettu: ${oo.suoritukset.map(
                            (foo) => foo.vahvistus?.päivä
                          )}`
                        : ''}
                    </span>
                  </Flex>
                </div>
              ))}
            </PlainList>
            {response.data.opiskeluoikeudet.length <= 0 && (
              <Trans>{'Suoritettuja tutkintoja ei löytynyt.'}</Trans>
            )}
          </div>
        </div>
      </ContentContainer>
    </>
  ) : (
    <Trans>{'Tietojen hakeminen epäonnistui'}</Trans>
  )
}

ReactDOM.render(<SuoritetutTutkinnot />, document.getElementById('content'))
