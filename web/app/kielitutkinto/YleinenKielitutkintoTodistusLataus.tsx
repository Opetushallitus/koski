import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  useApiMethod,
  useOnApiError,
  useOnApiSuccess,
  useSafeState
} from '../api-fetch/hooks'
import { isError, isSuccess } from '../api-fetch/apiUtils'
import { t } from '../i18n/i18n'
import { Spacer } from '../components-v2/layout/Spacer'
import { TodistusJob } from '../types/fi/oph/koski/todistus/TodistusJob'
import {
  fetchTodistusStatus,
  fetchTodistusStatusByJobId,
  generateTodistus
} from '../util/koskiApi'
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
import { TextWithLinks } from '../components-v2/texts/TextWithLinks'

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
  const [status, setStatus] = useSafeState<TodistusJob | null>(null)
  const [currentJobId, setCurrentJobId] = useSafeState<string | null>(null)

  const statusFetchByOid = useApiMethod(fetchTodistusStatus)
  const statusFetchByJobId = useApiMethod(fetchTodistusStatusByJobId)
  const generate = useApiMethod(generateTodistus)

  const pollStatusByJobId = useCallback(() => {
    if (currentJobId) {
      return statusFetchByJobId.call(currentJobId)
    }
  }, [currentJobId, statusFetchByJobId])

  const statePoller = useInterval(pollStatusByJobId, 2000)

  const startGenerating = useCallback(async () => {
    // Nollaa aiemmat virheet
    generate.clear()
    statusFetchByOid.clear()
    statusFetchByJobId.clear()
    setStatus(null)

    const result = await generate.call(language, opiskeluoikeusOid)
    // generate palauttaa heti statuksen, joten pollataan vain jos generointi on käynnissä
    if (
      result._tag === 'Right' &&
      result.right.data.state !== 'COMPLETED' &&
      result.right.data.state !== 'ERROR'
    ) {
      setCurrentJobId(result.right.data.id)
      statePoller.start()
    }
  }, [
    generate,
    statusFetchByOid,
    statusFetchByJobId,
    language,
    opiskeluoikeusOid,
    statePoller,
    setStatus,
    setCurrentJobId
  ])

  const updateStatusFromResponse = useCallback(
    (response: { data: TodistusJob }) => {
      // Päivitä status vain jos se vastaa nykyistä kieltä
      if (response.data.language === language) {
        const ignoredStates = ['EXPIRED', 'QUEUED_FOR_EXPIRE', 'INTERRUPTED']

        if (ignoredStates.includes(response.data.state)) {
          // Käsitellään ikään kuin jobia ei olisi koskaan ollut
          setStatus(null)
          setCurrentJobId(null)
          statePoller.stop()
          return
        }

        setStatus(response.data)
        if (
          response.data.state === 'COMPLETED' ||
          response.data.state === 'ERROR'
        ) {
          statePoller.stop()
        }
      }
    },
    [setStatus, setCurrentJobId, statePoller, language]
  )

  useOnApiSuccess(statusFetchByOid, updateStatusFromResponse)
  useOnApiSuccess(statusFetchByJobId, updateStatusFromResponse)
  useOnApiSuccess(generate, updateStatusFromResponse)
  useOnApiError(statusFetchByOid, () => {
    statePoller.stop()
    setStatus(null)
  })
  useOnApiError(statusFetchByJobId, () => {
    statePoller.stop()
    setStatus(null)
  })
  useOnApiError(generate, () => {
    statePoller.stop()
    setStatus(null)
  })

  // Hae todistuksen status kun komponentti mountataan tai kieli vaihtuu
  useEffect(() => {
    const fetchInitialStatus = async () => {
      statusFetchByOid.clear()
      statusFetchByJobId.clear()
      setStatus(null)
      setCurrentJobId(null)

      const result = await statusFetchByOid.call(language, opiskeluoikeusOid)

      if (result._tag === 'Right') {
        const job = result.right.data
        // Tilat jotka käsitellään ikään kuin jobia ei olisi koskaan ollut
        const ignoredStates = ['EXPIRED', 'QUEUED_FOR_EXPIRE', 'INTERRUPTED']

        if (ignoredStates.includes(job.state)) {
          // Ei tehdä mitään, näytetään tyhjä tila
          return
        }

        // Jos todistus on valmis tai virhetilassa, näytetään se suoraan
        if (job.state === 'COMPLETED' || job.state === 'ERROR') {
          // Status päivittyy updateStatusFromResponse-callbackissa
        } else {
          // Jos luonti on kesken, aloitetaan pollaus job-ID:llä
          setCurrentJobId(job.id)
          statePoller.start()
        }
      }
    }

    fetchInitialStatus()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [language, opiskeluoikeusOid])

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
        setCurrentJobId(null)
        statePoller.stop()
        generate.clear() // Nollaa myös generoinnin tila
        statusFetchByOid.clear()
        statusFetchByJobId.clear()
      }
    },
    [
      generate,
      statusFetchByOid,
      statusFetchByJobId,
      statePoller,
      setCurrentJobId,
      setStatus
    ]
  )

  const isInProgress =
    status && status.state !== 'COMPLETED' && status.state !== 'ERROR'

  const isCompleted = status && status.state === 'COMPLETED'

  const isLoadingStatus =
    statusFetchByOid.state === 'loading' ||
    statusFetchByOid.state === 'reloading' ||
    statusFetchByJobId.state === 'loading' ||
    statusFetchByJobId.state === 'reloading'

  const hasError = useMemo(() => {
    return status && status.state === 'ERROR'
  }, [status])

  const errorText = useMemo(() => {
    // Virhe TodistusJob:issa (state === 'ERROR')
    if (hasError) {
      return t('todistus:error')
    }
    // HTTP-virhe API-kutsuissa
    // Huom: statusFetchByOid:n virheet eivät näy käyttäjälle (initial fetch)
    if (isError(generate) || isError(statusFetchByJobId)) {
      return t('todistus:error')
    }
    return null
  }, [generate, statusFetchByJobId, hasError])

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
            allowOpenUpwards={true}
          />
        </div>
        {!isInProgress && !isCompleted && !isLoadingStatus && (
          <RaisedButton
            testId="start"
            onClick={startGenerating}
            disabled={generate.state === 'loading'}
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
            href={`/koski/todistus/download/${status.id}`}
            target="_blank"
            rel="noreferrer"
          >
            {t('Näytä todistus')}
          </a>
        )}
      </div>
      {errorText && (
        <TestIdText className="Todistus__error" id="error">
          <TextWithLinks>{errorText}</TextWithLinks>
        </TestIdText>
      )}
    </TestIdRoot>
  )
}
