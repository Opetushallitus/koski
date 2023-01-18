import React, { useCallback, useMemo, useState } from 'react'
import { ISO2FinnishDate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Vahvistus } from '../../types/fi/oph/koski/schema/Vahvistus'
import { isHenkilövahvistus } from '../../util/schema'
import { ClassOf } from '../../util/types'
import { common, CommonProps, CommonPropsWithChildren } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'
import { Trans } from '../texts/Trans'
import { SuorituksenVahvistusModal } from './SuorituksenVahvistusModal'

export type SuorituksenVahvistusViewProps<T extends Vahvistus> = CommonProps<
  FieldViewBaseProps<T | undefined>
>

export const SuorituksenVahvistusView = <T extends Vahvistus>({
  value,
  ...rest
}: SuorituksenVahvistusViewProps<T>) => (
  <SuorituksenVahvistus vahvistus={value} {...rest} />
)

export type SuorituksenVahvistusEditProps<T extends Vahvistus> = CommonProps<
  FieldEditBaseProps<T | undefined> & {
    vahvistusClass: ClassOf<T>
  }
>

export const SuorituksenVahvistusEdit = <T extends Vahvistus>({
  value,
  onChange,
  vahvistusClass,
  ...rest
}: SuorituksenVahvistusEditProps<T>) => {
  const [modalVisible, setModalVisible] = useState(false)

  const onMerkitseValmiiksi = useCallback(() => {
    setModalVisible(true)
  }, [])

  const onMerkitseKeskeneräiseksi = useCallback(() => {
    onChange(undefined)
  }, [onChange])

  return (
    <SuorituksenVahvistus vahvistus={value} {...rest}>
      {value ? (
        <FlatButton onClick={onMerkitseKeskeneräiseksi}>
          Merkitse keskeneräiseksi
        </FlatButton>
      ) : (
        <RaisedButton onClick={onMerkitseValmiiksi}>
          Merkitse valmiiksi
        </RaisedButton>
      )}
      {modalVisible && (
        <SuorituksenVahvistusModal vahvistusClass={vahvistusClass} />
      )}
    </SuorituksenVahvistus>
  )
}

type SuorituksenVahvistusProps = CommonPropsWithChildren<{
  vahvistus?: Vahvistus
}>

const SuorituksenVahvistus: React.FC<SuorituksenVahvistusProps> = (props) => {
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
      {props.children}
    </div>
  )
}
