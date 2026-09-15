import React from 'react'
import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import { useKoodisto } from '../appstate/koodisto'
import { TestIdText } from '../appstate/useTestId'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { t } from '../i18n/i18n'
import { AmmatillinenArviointi } from '../types/fi/oph/koski/schema/AmmatillinenArviointi'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { TutkinnonOsanLaajuus } from '../types/fi/oph/koski/tutkinto/TutkinnonOsanLaajuus'
import { fetchTutkinnonOsaRyhmienLaajuudet } from '../util/koskiApi'
import { formatNumber, sum } from '../util/numbers'
import {
  AmisTutkinnonOsanSuoritus,
  isAmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isAmisKorkeakouluopintoSuoritus
} from './tutkinnonOsanSuoritukset'

const cache = createPreferLocalCache(fetchTutkinnonOsaRyhmienLaajuudet)

// Perusteen tutkinnon osan ryhmien laajuusvaatimukset (min–max) ryhmän
// koodiarvon mukaan. Ryhmittelemättömälle perusteelle vaatimuksia ei ole.
export const useTutkinnonOsaRyhmienLaajuudet = (
  perusteenDiaarinumero: string | undefined,
  suoritustapa: string,
  ryhmät: string[]
): Record<string, TutkinnonOsanLaajuus> => {
  const params =
    perusteenDiaarinumero && ryhmät.length > 0
      ? ([perusteenDiaarinumero, suoritustapa, ryhmät] as [
          string,
          string,
          string[]
        ])
      : undefined
  const result = useApiWithParams(
    fetchTutkinnonOsaRyhmienLaajuudet,
    params,
    cache
  )
  return isSuccess(result) ? result.data : {}
}

type LaajuudellinenSuoritus = {
  koulutusmoduuli: {
    laajuus?: { arvo: number; yksikkö: { lyhytNimi?: LocalizedString } }
  }
  arviointi?: AmmatillinenArviointi[]
}

// Korkeakouluopintojen ja jatko-opintovalmiuksia tukevien opintojen laajuus
// lasketaan niiden osasuorituksista, kuten vanhassa käyttöliittymässä.
const laajuuteenLaskettavat = (
  suoritukset: AmisTutkinnonOsanSuoritus[]
): LaajuudellinenSuoritus[] =>
  suoritukset.flatMap((s): LaajuudellinenSuoritus[] =>
    isAmisKorkeakouluopintoSuoritus(s) ||
    isAmisJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(s)
      ? s.osasuoritukset || []
      : [s]
  )

// Sama sääntö kuin backendin AmmatillinenKoodistostaLöytyväArviointi.hyväksytty
const hyväksytystiArvioitu = (s: LaajuudellinenSuoritus): boolean => {
  const viimeisin = [...(s.arviointi || [])]
    .sort((a, b) => a.päivä.localeCompare(b.päivä))
    .at(-1)
  return (
    viimeisin !== undefined &&
    !['0', 'Hylätty'].includes(viimeisin.arvosana.koodiarvo)
  )
}

const laajuusväli = (laajuus?: TutkinnonOsanLaajuus): string | undefined => {
  if (laajuus?.min !== undefined && laajuus?.max !== undefined) {
    return laajuus.min === laajuus.max
      ? `${laajuus.max}`
      : `${laajuus.min}–${laajuus.max}`
  } else if (laajuus?.min !== undefined) {
    return `${laajuus.min}`
  } else if (laajuus?.max !== undefined) {
    return `-${laajuus.max}`
  }
  return undefined
}

type TutkinnonOsaRyhmänLaajuusProps = {
  suoritukset: AmisTutkinnonOsanSuoritus[]
  laajuus?: TutkinnonOsanLaajuus
  testId: string
}

export const TutkinnonOsaRyhmänLaajuus: React.FC<
  TutkinnonOsaRyhmänLaajuusProps
> = ({ suoritukset, laajuus, testId }) => {
  const hyväksytyt =
    laajuuteenLaskettavat(suoritukset).filter(hyväksytystiArvioitu)
  const yhteensä = sum(
    hyväksytyt.map((s) => s.koulutusmoduuli.laajuus?.arvo || 0)
  )
  // Tutkinnon osien laajuus on skeemassa aina osaamispisteinä (yksikkö 6)
  const laajuusyksiköt = useKoodisto('opintojenlaajuusyksikko')
  const yksikkö =
    laajuuteenLaskettavat(suoritukset).find((s) => s.koulutusmoduuli.laajuus)
      ?.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi ||
    laajuusyksiköt?.find((k) => k.koodiviite.koodiarvo === '6')?.koodiviite
      .lyhytNimi
  const väli = laajuusväli(laajuus)

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Yhteensä">
        <TestIdText id={testId}>
          {[formatNumber(yhteensä), väli && `/ ${väli}`, yksikkö && t(yksikkö)]
            .filter(Boolean)
            .join(' ')}
        </TestIdText>
      </KeyValueRow>
    </KeyValueTable>
  )
}
