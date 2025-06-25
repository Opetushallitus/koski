import React from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { TestIdText } from '../appstate/useTestId'
import { formatNumber, sum } from '../util/numbers'
import { t } from '../i18n/i18n'
import { AmmatillisenTutkinnonOsittainenSuoritus } from '../types/fi/oph/koski/schema/AmmatillisenTutkinnonOsittainenSuoritus'

type AmisLaajuudetYhteensäProps = {
  suoritus: AmmatillisenTutkinnonOsittainenSuoritus
}

export const AmisLaajuudetYhteensä: React.FC<AmisLaajuudetYhteensäProps> = ({
  suoritus
}) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Yhteensä">
        <TestIdText id="yhteensa">{laajuudetYhteensä(suoritus)}</TestIdText>
      </KeyValueRow>
    </KeyValueTable>
  )
}

const laajuudetYhteensä = (
  pts: AmmatillisenTutkinnonOsittainenSuoritus
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
