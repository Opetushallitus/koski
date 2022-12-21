import { isNonEmpty } from 'fp-ts/lib/Array'
import * as $ from 'optics-ts'
import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { Laajuus } from '../../types/fi/oph/koski/schema/Laajuus'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { removeFloatingPointDrift } from '../../util/numbers'
import { CollectableOptic } from '../../util/types'
import { baseProps, BaseProps } from '../baseProps'
import { NumberField } from '../controls/NumberField'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormModel'

/* ---------------------------------------------------------------------
 *
 * LaajuusView, geneerinen laajuustiedon näyttävä komponentti
 *
 * ---------------------------------------------------------------------
 */

export type LaajuusViewProps = BaseProps & FieldViewBaseProps<Laajuus>

export const LaajuusView = (props: LaajuusViewProps) => {
  return props.value ? (
    <span {...baseProps(props)}>
      {props.value.arvo}{' '}
      {t(props.value.yksikkö.lyhytNimi || props.value.yksikkö.nimi)}
    </span>
  ) : null
}

export type LaajuusEditProps<T extends Laajuus> = BaseProps &
  FieldEditBaseProps<T> & {
    createLaajuus: (arvo: number) => T
  }

/* ---------------------------------------------------------------------
 *
 * LaajuusEdit, geneerinen laajuustiedon muokkauskomponentti
 *
 * ---------------------------------------------------------------------
 */

export const LaajuusEdit = <T extends Laajuus>(props: LaajuusEditProps<T>) => {
  const onChange = useCallback(
    (arvo: number) => props.onChange(props.createLaajuus(arvo)),
    [props.onChange, props.createLaajuus]
  )

  return (
    <label {...baseProps(props, 'LaajuusField')}>
      <NumberField
        className="LaajuusField__arvo"
        value={props.value?.arvo}
        onChange={onChange}
      />
      <span className="LaajuusField__yksikko">
        {t(props.value?.yksikkö.lyhytNimi || props.value?.yksikkö.nimi)}
      </span>
    </label>
  )
}

/* ---------------------------------------------------------------------
 *
 * LaajuusEditiä spesifimmät version erilaisille laajuusyksiköille
 *
 * ---------------------------------------------------------------------
 */

export type DefaultLaajuusEditProps<T extends Laajuus> = BaseProps &
  FieldEditBaseProps<T>

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
