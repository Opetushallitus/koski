import React from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
import { formatNumber, sum } from '../../util/numbers'
import { t } from '../../i18n/i18n'
import { TestIdText } from '../../appstate/useTestId'
import { VapaanSivistystyönKoulutuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonKoulutuksenPaatasonSuoritus'

export type VSTLaajuudetYhteensäProps = {
  testId: string
  suoritus: VapaanSivistystyönKoulutuksenPäätasonSuoritus
}

export const VSTLaajuudetYhteensä: React.FC<VSTLaajuudetYhteensäProps> = ({
  suoritus,
  testId
}) => (
  <KeyValueTable>
    <KeyValueRow localizableLabel="Yhteensä">
      <TestIdText id="yhteensa">{laajuudetYhteensä(suoritus)}</TestIdText>
    </KeyValueRow>
  </KeyValueTable>
)

const laajuudetYhteensä = (
  pts: VapaanSivistystyönKoulutuksenPäätasonSuoritus
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
