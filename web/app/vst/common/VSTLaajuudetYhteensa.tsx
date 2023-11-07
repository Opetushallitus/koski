import React from 'react'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { formatNumber, sum } from '../../util/numbers'
import { t } from '../../i18n/i18n'

export type VSTLaajuudetYhteensäProps = {
  testId: string
  suoritus: VapaanSivistystyönPäätasonSuoritus
}

export const VSTLaajuudetYhteensä: React.FC<VSTLaajuudetYhteensäProps> = ({
  suoritus,
  testId
}) => (
  <KeyValueTable>
    <KeyValueRow label="Yhteensä" testId={`${testId}.yhteensa`}>
      {laajuudetYhteensä(suoritus)}
    </KeyValueRow>
  </KeyValueTable>
)

const laajuudetYhteensä = (pts: VapaanSivistystyönPäätasonSuoritus): string => {
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
