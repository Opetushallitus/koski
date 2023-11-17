import {
  mapInitial,
  mapLoading,
  mapSuccess,
  mapError,
  useApiWithParams
} from './api-fetch'
import { fetchSuoritetutTutkinnot } from './util/koskiApi'
import * as R from 'ramda'
import React from 'react'
import ReactDOM from 'react-dom'
import { SuoritusjakoTopBar } from './components-v2/layout/SuoritusjakoTopBar'
import { ContentContainer } from './components-v2/containers/ContentContainer'
import { PlainList } from './components-v2/containers/PlainList'
import { Trans } from './components-v2/texts/Trans'
import { Flex } from './components-v2/containers/Flex'
import { isSuoritetutTutkinnotOpiskeluoikeus } from './types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotOpiskeluoikeus'
import { isSuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus } from './types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus'
import Text from './i18n/Text'
import { SuoritetutTutkinnotOppija } from './types/fi/oph/koski/suoritusjako/suoritetuttutkinnot/SuoritetutTutkinnotOppija'
// @ts-ignore
__webpack_nonce__ = window.nonce
// @ts-ignore
import(/* webpackChunkName: "styles" */ './style/main.less')

const secret = R.last(document.location.pathname.split('/')) ?? ''

const SuoritetutTutkinnot = () => {
  const response = useApiWithParams(fetchSuoritetutTutkinnot, [secret])
  return (
    <>
      {mapInitial(response, () => (
        <Trans>{'Haetaan'}</Trans>
      ))}
      {mapLoading(response, () => (
        <Trans>{'Haetaan'}</Trans>
      ))}
      {mapError(response, () => (
        <Trans>{'Tietojen hakeminen epäonnistui'}</Trans>
      ))}
      {mapSuccess(response, (responseData: SuoritetutTutkinnotOppija) => (
        <>
          <SuoritusjakoTopBar />
          <ContentContainer className="content-area suoritusjako suoritusjako-page">
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
                    {responseData.henkilö.etunimet}{' '}
                    {responseData.henkilö.sukunimi}
                  </p>
                </div>
              </div>

              <div></div>

              <div>
                <p>
                  <Trans>
                    {
                      'Tässä listataan suoritetut tutkinnot otsikkotasolla. Tarkemmat tiedot löytyvät koneluettavassa muodossa yllä olevasta linkistä'
                    }
                  </Trans>
                </p>
                <PlainList>
                  {responseData.opiskeluoikeudet.map((oo, index) => (
                    <div
                      key={oo.$class + '-' + index}
                      className={'suoritettu-tutkinto'}
                    >
                      <h3>
                        {oo.oppilaitos ? (
                          <Trans>{oo.oppilaitos.nimi}</Trans>
                        ) : oo.koulutustoimija ? (
                          <Trans>{oo.koulutustoimija.nimi}</Trans>
                        ) : (
                          <>
                            {/* @ts-expect-error Text */}
                            <Text
                              name={
                                'Oppilaitos tai koulutustoimija ei tiedossa'
                              }
                            />
                          </>
                        )}
                      </h3>{' '}
                      <Flex>
                        <span>
                          <Trans>{oo.tyyppi.nimi}</Trans>
                          {': '}
                          {oo.suoritukset.map((s, i) => (
                            <>
                              {i > 0 && ', '}
                              <Trans>{s.koulutusmoduuli.tunniste.nimi}</Trans>
                            </>
                          ))}
                        </span>
                        <span>
                          {isSuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus(
                            oo
                          )
                            ? `Opiskeluoikeuden oid: ${oo.oid}`
                            : ''}
                        </span>
                        <span>
                          {isSuoritetutTutkinnotOpiskeluoikeus(oo)
                            ? `Vahvistettu: ${oo.suoritukset.map(
                                (foo) => foo.vahvistus?.päivä
                              )}`
                            : ''}
                        </span>
                      </Flex>
                    </div>
                  ))}
                </PlainList>
                {responseData.opiskeluoikeudet.length <= 0 && (
                  <Trans>{'Suoritettuja tutkintoja ei löytynyt'}</Trans>
                )}
              </div>
            </div>
          </ContentContainer>
        </>
      ))}
    </>
  )
}

ReactDOM.render(<SuoritetutTutkinnot />, document.getElementById('content'))
