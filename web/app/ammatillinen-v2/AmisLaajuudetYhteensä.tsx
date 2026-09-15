import React from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { TestIdText } from '../appstate/useTestId'
import { formatNumber, sum } from '../util/numbers'
import { t } from '../i18n/i18n'
import { AmisTutkinnonSuoritus } from './tutkinnonOsanSuoritukset'
import { AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'

type AmisLaajuudetYhteensäProps = {
  suoritus:
    | AmisTutkinnonSuoritus
    | AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
}

export const AmisLaajuudetYhteensä: React.FC<AmisLaajuudetYhteensäProps> = ({
  suoritus
}) => {
  if (!hasLaajuus(suoritus)) return null

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Yhteensä">
        <TestIdText id="yhteensa">{laajuudetYhteensä(suoritus)}</TestIdText>
      </KeyValueRow>
    </KeyValueTable>
  )
}

const hasLaajuus = (
  pts:
    | AmisTutkinnonSuoritus
    | AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
): boolean =>
  sum(
    (pts.osasuoritukset || []).map(
      (os) => os.koulutusmoduuli.laajuus?.arvo || 0
    )
  ) > 0

const laajuudetYhteensä = (
  pts:
    | AmisTutkinnonSuoritus
    | AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus
): string => {
  const n = formatNumber(
    sum(
      (pts.osasuoritukset || []).map(
        (os) => os.koulutusmoduuli.laajuus?.arvo || 0
      )
    )
  )
  const yksikkö =
    pts.osasuoritukset?.[0]?.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi || ''

  return `${n} ${t(yksikkö)}`
}
