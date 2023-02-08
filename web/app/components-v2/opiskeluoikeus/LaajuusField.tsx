import { isNonEmpty } from 'fp-ts/lib/Array'
import * as $ from 'optics-ts'
import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { Laajuus } from '../../types/fi/oph/koski/schema/Laajuus'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { removeFloatingPointDrift } from '../../util/numbers'
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

export type LaajuusViewProps = CommonProps<FieldViewerProps<Laajuus>>

export const LaajuusView = (props: LaajuusViewProps) => {
  return props.value ? (
    <span {...common(props)}>
      {props.value.arvo}{' '}
      {t(props.value.yksikkö.lyhytNimi || props.value.yksikkö.nimi)}
    </span>
  ) : null
}

export type LaajuusEditProps<T extends Laajuus> = CommonProps<
  FieldEditorProps<T> & {
    createLaajuus: (arvo: number) => T
  }
>

/* ---------------------------------------------------------------------
 *
 * LaajuusEdit, geneerinen laajuustiedon muokkauskomponentti
 *
 * ---------------------------------------------------------------------
 */

export const LaajuusEdit = <T extends Laajuus>(props: LaajuusEditProps<T>) => {
  const { onChange, createLaajuus } = props
  const onChangeCB = useCallback(
    (arvo: number) => onChange(createLaajuus(arvo)),
    [createLaajuus, onChange]
  )

  return (
    <label {...common(props, ['LaajuusField'])}>
      <div className="LaajuusField__container">
        <NumberField
          className="LaajuusField__arvo"
          value={props.value?.arvo}
          onChange={onChangeCB}
          hasErrors={Boolean(props.errors)}
        />
        <span className="LaajuusField__yksikko">
          {t(props.value?.yksikkö.lyhytNimi || props.value?.yksikkö.nimi)}
        </span>
      </div>
      <FieldErrors errors={props.errors} />
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
  FieldEditorProps<T>
>

export const LaajuusOpintopisteissäEdit: React.FC<
  DefaultLaajuusEditProps<LaajuusOpintopisteissä>
> = (props) => (
  <LaajuusEdit
    {...props}
    createLaajuus={(arvo) => LaajuusOpintopisteissä({ arvo })}
  />
)

/* ---------------------------------------------------------------------
 *
 * Apufunktiot käytettäväksi form.render() auto-kentän kanssa
 *
 * ---------------------------------------------------------------------
 */

export const laajuusSum =
  <S, A extends Laajuus>(laajuusPath: CollectableOptic<S, A>, data: S) =>
  (): A | undefined => {
    const laajuudet = $.collect(laajuusPath)(data)
    return isNonEmpty(laajuudet)
      ? {
          ...laajuudet[0],
          arvo: removeFloatingPointDrift(
            laajuudet.reduce((acc, laajuus) => acc + laajuus.arvo, 0)
          )
        }
      : undefined
  }
