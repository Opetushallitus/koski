import { isNonEmpty } from 'fp-ts/lib/Array'
import * as $ from 'optics-ts'
import React, { useCallback, useEffect } from 'react'
import { useSafeState } from '../../api-fetch'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { Laajuus } from '../../types/fi/oph/koski/schema/Laajuus'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { nonNull } from '../../util/fp/arrays'
import { formatNumber, removeFloatingPointDrift } from '../../util/numbers'
import { EmptyObject } from '../../util/objects'
import { CollectableOptic } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { NumberField } from '../controls/NumberField'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

/* ---------------------------------------------------------------------
 *
 * LaajuusView, geneerinen laajuustiedon näyttävä komponentti
 *
 * ---------------------------------------------------------------------
 */

export type LaajuusViewProps = CommonProps<
  FieldViewerProps<Laajuus, EmptyObject>
>

export const LaajuusView = (props: LaajuusViewProps) => {
  return (
    <TestIdText {...common(props)} id="laajuus.value">
      {props.value
        ? formatNumber(props.value.arvo) +
          ' ' +
          t(props.value.yksikkö.lyhytNimi || props.value.yksikkö.nimi)
        : '–'}
    </TestIdText>
  )
}

export type LaajuusEditProps<T extends Laajuus> = CommonProps<
  FieldEditorProps<
    T,
    {
      createLaajuus: (arvo: number) => T
    }
  >
>

/* ---------------------------------------------------------------------
 *
 * LaajuusEdit, geneerinen laajuustiedon muokkauskomponentti
 *
 * ---------------------------------------------------------------------
 */

export const LaajuusEdit = <T extends Laajuus>(props: LaajuusEditProps<T>) => {
  const fillNimi = useKoodistoFiller()
  const { onChange, createLaajuus } = props
  const onChangeCB = useCallback(
    (arvo: number) => onChange(createLaajuus(arvo)),
    [createLaajuus, onChange]
  )

  const [yksikönNimi, setYksikönNimi] = useSafeState<string>('')

  useEffect(() => {
    const laajuus = props.value || createLaajuus(0)
    if (laajuus.yksikkö.nimi || laajuus.yksikkö.lyhytNimi) {
      setYksikönNimi(t(laajuus.yksikkö.lyhytNimi || laajuus.yksikkö.nimi) || '')
    } else {
      fillNimi(laajuus).then((l) =>
        setYksikönNimi(t(l.yksikkö.lyhytNimi || l.yksikkö.nimi) || '')
      )
    }
  }, [createLaajuus, fillNimi, props.value, setYksikönNimi])

  return (
    <label {...common(props, ['LaajuusField'])}>
      <div className="LaajuusField__container">
        <NumberField
          className="LaajuusField__arvo"
          value={props.value?.arvo}
          onChange={onChangeCB}
          hasErrors={Boolean(props.errors)}
          testId="laajuus.edit"
        />
        <TestIdText id="laajuus.unit" className="LaajuusField__yksikko">
          {yksikönNimi}
        </TestIdText>
      </div>
      <TestIdLayer id="laajuus.edit">
        <FieldErrors errors={props.errors} />
      </TestIdLayer>
    </label>
  )
}

/* ---------------------------------------------------------------------
 *
 * LaajuusEditiä spesifimmät version erilaisille laajuusyksiköille
 *
 * ---------------------------------------------------------------------
 */

export type DefaultLaajuusEditProps<T extends Laajuus> = CommonProps<
  FieldEditorProps<T, EmptyObject>
>

export const LaajuusOpintopisteissäEdit: React.FC<
  DefaultLaajuusEditProps<LaajuusOpintopisteissä>
> = (props) => (
  <LaajuusEdit
    {...props}
    createLaajuus={(arvo: any) => LaajuusOpintopisteissä({ arvo }) as any}
  />
)

export const LaajuusKursseissaEdit: React.FC<
  DefaultLaajuusEditProps<LaajuusOpintopisteissä>
> = (props) => (
  <LaajuusEdit
    {...props}
    createLaajuus={(arvo: any) => LaajuusKursseissa({ arvo }) as any}
  />
)

/* ---------------------------------------------------------------------
 *
 * Apufunktiot käytettäväksi form.render() auto-kentän kanssa
 *
 * ---------------------------------------------------------------------
 */

export const laajuusSum =
  <S, A extends Laajuus | undefined>(
    laajuusPath: CollectableOptic<S, A>,
    data: S
  ) =>
  (): A | undefined => {
    const laajuudet = $.collect(laajuusPath)(data).filter(nonNull)
    return isNonEmpty(laajuudet)
      ? {
          ...laajuudet[0],
          arvo: removeFloatingPointDrift(
            laajuudet.reduce((acc, laajuus) => acc + (laajuus?.arvo ?? 0), 0)
          )
        }
      : undefined
  }
