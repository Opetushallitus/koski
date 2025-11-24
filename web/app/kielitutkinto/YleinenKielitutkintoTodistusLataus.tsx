import React, { useCallback, useEffect, useState } from 'react'
import {
  useApiMethod,
  useApiWithParams,
  useOnApiSuccess
} from '../api-fetch/hooks'
import { isSuccess } from '../api-fetch/apiUtils'
import { t } from '../i18n/i18n'
import { Spacer } from '../components-v2/layout/Spacer'
import { TodistusJob } from '../types/fi/oph/koski/todistus/TodistusJob'
import { fetchTodistusStatus, generateTodistus } from '../util/koskiApi'
import { useInterval } from '../util/useInterval'
import { TestIdRoot, TestIdText } from '../appstate/useTestId'
import {
  Select,
  SelectOption,
  OptionList
} from '../components-v2/controls/Select'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Spinner } from '../components-v2/texts/Spinner'
import { Trans } from '../components-v2/texts/Trans'

export type TodistusLanguage = 'fi' | 'sv' | 'en'

export type YleinenKielitutkintoTodistusLatausProps = {
  opiskeluoikeusOid: string
}

const kielitutkintoTodistusLanguages: OptionList<TodistusLanguage> = [
  { key: 'fi', value: 'fi', label: t('Fi') },
  { key: 'sv', value: 'sv', label: t('Sv') },
  { key: 'en', value: 'en', label: t('En') }
]

export const YleinenKielitutkintoTodistusLataus: React.FC<
  YleinenKielitutkintoTodistusLatausProps
> = ({ opiskeluoikeusOid }) => {
  const [language, setLanguage] = useState<TodistusLanguage>('fi')
  const [status, setStatus] = useState<TodistusJob | null>(null)

  const statusFetch = useApiWithParams(fetchTodistusStatus, [
    language,
    opiskeluoikeusOid
  ])

  const generate = useApiMethod(generateTodistus)

  const pollStatus = useCallback(
    () => statusFetch.call(language, opiskeluoikeusOid),
    [language, opiskeluoikeusOid, statusFetch]
  )

  const statePoller = useInterval(pollStatus, 2000)

  const startGenerating = useCallback(async () => {
    await generate.call(language, opiskeluoikeusOid)
    await pollStatus()
    statePoller.start()
  }, [generate, language, opiskeluoikeusOid, pollStatus, statePoller])

  const updateStatusFromResponse = useCallback(
    (response: { data: TodistusJob }) => {
      // Päivitä status vain jos se vastaa nykyistä kieltä
      if (response.data.language === language) {
        setStatus(response.data)
        if (
          response.data.state === 'COMPLETED' ||
          response.data.state === 'ERROR'
        ) {
          statePoller.stop()
        }
      }
    },
    [statePoller, language]
  )

  useOnApiSuccess(statusFetch, updateStatusFromResponse)
  useOnApiSuccess(generate, updateStatusFromResponse)

  useEffect(() => {
    return () => {
      statePoller.stop()
    }
  }, [statePoller])

  const handleLanguageChange = useCallback(
    (option?: SelectOption<TodistusLanguage>) => {
      if (option?.value) {
        setLanguage(option.value)
        setStatus(null)
        statePoller.stop()
        generate.clear() // Nollaa myös generoinnin tila
      }
    },
    [generate, statePoller]
  )

  const isInProgress =
    status && status.state !== 'COMPLETED' && status.state !== 'ERROR'

  const isCompleted = status && status.state === 'COMPLETED'

  const isLoadingStatus =
    statusFetch.state === 'loading' || statusFetch.state === 'reloading'

  return (
    <TestIdRoot id="kielitutkintoTodistus">
      <Spacer />
      <div className="Todistus">
        <span className="Todistus__title">{'TODISTUS'}</span>
        <div className="Todistus__lang">
          <div className="Todistus__langLabel">
            <Trans>{'Todistuksen kieli'}</Trans>
          </div>
          {':'}
          <Select
            className="Todistus__langSelect"
            testId="language"
            options={kielitutkintoTodistusLanguages}
            value={language}
            onChange={handleLanguageChange}
            disabled={!!isInProgress}
          />
        </div>
        {!isInProgress && !isCompleted && !isLoadingStatus && (
          <RaisedButton
            testId="start"
            onClick={startGenerating}
            disabled={isSuccess(generate)}
          >
            {t('Lataa todistus')}
          </RaisedButton>
        )}
        {isInProgress && (
          <>
            <Spinner inline compact />
            <TestIdText id="loading">
              <Trans>{'Ladataan todistusta...'}</Trans>
            </TestIdText>
          </>
        )}
        {isCompleted && status && (
          <a
            data-testid="kielitutkintoTodistus.open"
            href={`/koski/api/todistus/download/${status.id}`}
            target="_blank"
            rel="noreferrer"
          >
            {t('Näytä todistus')}
          </a>
        )}
      </div>
    </TestIdRoot>
  )
}
