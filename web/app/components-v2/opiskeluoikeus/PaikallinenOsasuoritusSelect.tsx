import React, { useCallback, useMemo, useState } from 'react'
import { useSchema } from '../../appstate/constraints'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { allLanguages } from '../../util/optics'
import { common, CommonProps, subTestId } from '../CommonProps'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TextEdit, TextView } from '../controls/TextField'
import { FormField, sideUpdate } from '../forms/FormField'
import { useForm } from '../forms/FormModel'

const NEW_KEY = '__NEW__'

export type PaikallinenOsasuoritusSelectProps = CommonProps<{
  tunnisteet?: PaikallinenKoodi[]
  addNewText?: string | LocalizedString
  onSelect: (tunniste: PaikallinenKoodi, isNew: boolean) => void
  onRemove?: (tunniste: PaikallinenKoodi) => void
}>

export const PaikallinenOsasuoritusSelect: React.FC<
  PaikallinenOsasuoritusSelectProps
> = (props) => {
  const [modalIsVisible, setModalVisible] = useState(false)

  const hideModal = useCallback(() => {
    return setModalVisible(false)
  }, [])

  const options: OptionList<PaikallinenKoodi> = useMemo(
    () => [
      {
        key: NEW_KEY,
        label: t('Lisää osasuoritus'),
        value: emptyPaikallinenKoodi,
        ignoreFilter: true
      },
      ...(props.tunnisteet || []).map((tunniste) => ({
        key: tunniste.koodiarvo,
        label: t(tunniste.nimi),
        value: tunniste,
        removable: true
      }))
    ],
    [props.tunnisteet]
  )

  const { onSelect } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<PaikallinenKoodi>) => {
      if (option?.key === NEW_KEY) {
        setModalVisible(true)
      } else if (option?.value) {
        onSelect(option.value, false)
      }
    },
    [onSelect]
  )

  const onCreateNew = useCallback(
    (tunniste: PaikallinenKoodi) => {
      setModalVisible(false)
      onSelect(tunniste, true)
    },
    [onSelect]
  )

  const { onRemove } = props
  const onRemoveCB = useCallback(
    (option: SelectOption<PaikallinenKoodi>) =>
      option.value && onRemove?.(option.value),
    [onRemove]
  )

  return (
    <>
      <Select
        placeholder={props.addNewText || t('Lisää osasuoritus')}
        options={options}
        hideEmpty
        onChange={onChangeCB}
        onRemove={onRemoveCB}
        testId={subTestId(props, 'select')}
      />
      {modalIsVisible && (
        <UusiOsasuoritusModal
          onClose={hideModal}
          onSubmit={onCreateNew}
          testId={subTestId(props, 'modal')}
        />
      )}
    </>
  )
}

const emptyPaikallinenKoodi = PaikallinenKoodi({
  koodiarvo: '',
  nimi: localize('')
})

type UusiOsasuoritusModalProps = CommonProps<{
  onClose: () => void
  onSubmit: (paikallinenKoodi: PaikallinenKoodi) => void
}>

const UusiOsasuoritusModal: React.FC<UusiOsasuoritusModalProps> = (props) => {
  const paikallinenKoodiSchema = useSchema('PaikallinenKoodi')
  const form = useForm(emptyPaikallinenKoodi, true, paikallinenKoodiSchema)
  const koodiarvoPath = form.root.prop('koodiarvo')

  const { onSubmit } = props
  const onSubmitCB = useCallback(() => {
    onSubmit(form.state)
  }, [onSubmit, form.state])

  const updateOsasuoritusNimi = useMemo(
    () =>
      sideUpdate<PaikallinenKoodi, string, string>(
        form.root.prop('nimi').compose(allLanguages),
        (koodiarvo) => koodiarvo || ''
      ),
    [form.root]
  )

  return (
    <Modal {...common(props)} onClose={props.onClose}>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        <FormField
          form={form}
          path={koodiarvoPath}
          updateAlso={[updateOsasuoritusNimi]}
          errorsFromPath="nimi"
          view={TextView}
          edit={(editProps) => (
            <TextEdit
              {...editProps}
              placeholder={t('Osasuorituksen nimi')}
              autoFocus
              testId={subTestId(props, 'nimi.edit')}
            />
          )}
        />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose} testId={subTestId(props, 'cancel')}>
          {'Peruuta'}
        </FlatButton>
        <RaisedButton
          disabled={!form.isValid}
          onClick={onSubmitCB}
          testId={subTestId(props, 'submit')}
        >
          {'Lisää'}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
