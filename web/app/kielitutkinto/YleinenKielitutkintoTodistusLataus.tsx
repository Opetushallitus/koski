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
import { useVirkailijaUser } from '../appstate/user'

export type TodistusLanguage = 'fi' | 'sv' | 'en'

export type TodistusTemplateVariant =
  | 'fi'
  | 'sv'
  | 'en'
  | 'fi_tulostettava_uusi'
  | 'fi_tulostettava_paivitys'
  | 'sv_tulostettava_uusi'
  | 'sv_tulostettava_paivitys'
  | 'en_tulostettava_uusi'
  | 'en_tulostettava_paivitys'

export type YleinenKielitutkintoTodistusLatausProps = {
  opiskeluoikeusOid: string
}

type TodistusPdfTemplate =
  | 'digitaalinen'
  | 'tulostettava_uusi'
  | 'tulostettava_paivitys'

const languageOptions: OptionList<TodistusLanguage> = [
  { key: 'fi', value: 'fi', label: t('todistus:language:fi') },
  { key: 'sv', value: 'sv', label: t('todistus:language:sv') },
  { key: 'en', value: 'en', label: t('todistus:language:en') }
]

const pdfTemplateOptions: OptionList<TodistusPdfTemplate> = [
  {
    key: 'digitaalinen',
    value: 'digitaalinen',
    label: t('todistus:pdfTemplate:digitaalinen')
  },
  {
    key: 'tulostettava_uusi',
    value: 'tulostettava_uusi',
    label: t('todistus:pdfTemplate:tulostettava_uusi')
  },
  {
    key: 'tulostettava_paivitys',
    value: 'tulostettava_paivitys',
    label: t('todistus:pdfTemplate:tulostettava_paivitys')
  }
]

const buildTemplateVariant = (
  language: TodistusLanguage,
  pdfTemplate: TodistusPdfTemplate
): TodistusTemplateVariant => {
  if (pdfTemplate === 'digitaalinen') {
    return language
  }
  return `${language}_${pdfTemplate}` as TodistusTemplateVariant
}

export const YleinenKielitutkintoTodistusLataus: React.FC<
  YleinenKielitutkintoTodistusLatausProps
> = ({ opiskeluoikeusOid }) => {
  const hasPääkäyttäjäAccess = useVirkailijaUser()?.hasPääkäyttäjäAccess

  const [language, setLanguage] = useState<TodistusLanguage>('fi')
  const [pdfTemplate, setPdfTemplate] =
    useState<TodistusPdfTemplate>('digitaalinen')

  const templateVariant = useMemo(
    () => buildTemplateVariant(language, pdfTemplate),
    [language, pdfTemplate]
  )

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

    const result = await generate.call(templateVariant, opiskeluoikeusOid)
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
    templateVariant,
    opiskeluoikeusOid,
    statePoller,
    setStatus,
    setCurrentJobId
  ])

  const updateStatusFromResponse = useCallback(
    (response: { data: TodistusJob }) => {
      // Päivitä status vain jos se vastaa nykyistä varianttia
      if (response.data.templateVariant === templateVariant) {
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
    [setStatus, setCurrentJobId, statePoller, templateVariant]
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

      const result = await statusFetchByOid.call(
        templateVariant,
        opiskeluoikeusOid
      )

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
  }, [templateVariant, opiskeluoikeusOid])

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
        generate.clear()
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

  const handlePdfTemplateChange = useCallback(
    (option?: SelectOption<TodistusPdfTemplate>) => {
      if (option?.value) {
        setPdfTemplate(option.value)
        setStatus(null)
        setCurrentJobId(null)
        statePoller.stop()
        generate.clear()
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
            {':'}
          </div>
          <Select
            className="Todistus__langSelect"
            testId="language"
            options={languageOptions}
            value={language}
            onChange={handleLanguageChange}
            disabled={!!isInProgress}
            allowOpenUpwards={true}
          />
        </div>
        {hasPääkäyttäjäAccess && (
          <div className="Todistus__pdfTemplate">
            <div className="Todistus__pdfTemplateLabel">
              <Trans>{'PDF-pohja'}</Trans>
              {':'}
            </div>
            <Select
              className="Todistus__pdfTemplateSelect"
              testId="pdfTemplate"
              options={pdfTemplateOptions}
              value={pdfTemplate}
              onChange={handlePdfTemplateChange}
              disabled={!!isInProgress}
              allowOpenUpwards={true}
            />
          </div>
        )}
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
        {hasPääkäyttäjäAccess && (
          <a
            data-testid="kielitutkintoTodistus.openPreview"
            href={`/koski/todistus/preview/${templateVariant}/${opiskeluoikeusOid}`}
            target="_blank"
            rel="noreferrer"
          >
            {t('todistus:preview')}
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
