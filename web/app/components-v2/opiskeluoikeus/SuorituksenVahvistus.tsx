import React, { useMemo } from 'react'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Vahvistus } from '../../types/fi/oph/koski/schema/Vahvistus'
import { isHenkilövahvistus } from '../../util/schema'
import { common, CommonProps } from '../CommonProps'
import { Trans } from '../texts/Trans'

export type SuorituksenVahvistusProps = CommonProps<{
  vahvistus?: Vahvistus
}>

export const SuorituksenVahvistus: React.FC<SuorituksenVahvistusProps> = (
  props
) => {
  const { vahvistus } = props
  const myöntäjäHenkilöt = useMemo(
    () =>
      vahvistus && isHenkilövahvistus(vahvistus)
        ? vahvistus.myöntäjäHenkilöt.map(
            (h) => `${h.nimi}${h.titteli ? ` (${t(h.titteli)})` : ''}`
          )
        : [],
    [vahvistus]
  )

  return (
    <div
      {...common(props, [
        'SuorituksenVahvistus',
        vahvistus && 'SuorituksenVahvistus--valmis'
      ])}
    >
      <div className="SuorituksenVahvistus__status">
        {vahvistus ? t('Suoritus valmis') : t('Suoritus kesken')}
      </div>
      {vahvistus && (
        <>
          <div className="SuorituksenVahvistus__vahvistus">
            <Trans>Vahvistus</Trans>: {ISO2FinnishDate(vahvistus.päivä)}{' '}
            {t(vahvistus.myöntäjäOrganisaatio.nimi)}
          </div>
          {myöntäjäHenkilöt.map((myöntäjä, i) => (
            <div key={i} className="SuorituksenVahvistus__myontaja">
              {myöntäjä}
            </div>
          ))}
        </>
      )}
    </div>
  )
}
