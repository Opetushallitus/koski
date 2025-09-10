import React from 'react'
import { FormModel } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { SisältäväOpiskeluoikeus } from '../types/fi/oph/koski/schema/SisaltavaOpiskeluoikeus'
import { CommonProps } from '../components-v2/CommonProps'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../components-v2/forms/FormField'
import { EmptyObject } from '../util/objects'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { t } from '../i18n/i18n'
import { TextEdit } from '../components-v2/controls/TextField'
import { OrganisaatioView } from '../components-v2/opiskeluoikeus/OrganisaatioField'
import { OppilaitosSelect } from '../uusiopiskeluoikeus/components/OppilaitosSelect'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import {
  BooleanEdit,
  BooleanView
} from '../components-v2/opiskeluoikeus/BooleanField'
import { CHARCODE_REMOVE } from '../components-v2/texts/Icon'
import { IconButton } from '../components-v2/controls/IconButton'

interface SisältyyOpiskeluoikeuteenProps {
  form: FormModel<AmmatillinenOpiskeluoikeus>
}

const emptySisältäväOpiskeluoikeus: SisältäväOpiskeluoikeus =
  SisältäväOpiskeluoikeus({
    oid: '',
    oppilaitos: Oppilaitos({ oid: '' })
  })

export const SisältyyOpiskeluoikeuteen: React.FC<
  SisältyyOpiskeluoikeuteenProps
> = ({ form }) => {
  const path = form.root.prop('sisältyyOpiskeluoikeuteen')

  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel={'Ostettu'}>
        <FormField
          form={form}
          view={BooleanView}
          viewProps={{ hideFalse: true }}
          edit={BooleanEdit}
          path={form.root.prop('ostettu')}
        />
      </KeyValueRow>
      <KeyValueRow localizableLabel={'Sisältyy opiskeluoikeuteen'}>
        {form.state.sisältyyOpiskeluoikeuteen ? (
          <FormField
            form={form}
            view={SisältyyOpiskeluoikeuteenView}
            edit={SisältyyOpiskeluoikeuteenEdit}
            path={path}
          />
        ) : (
          form.editMode && (
            <ButtonGroup>
              <FlatButton
                onClick={() =>
                  form.updateAt(path, () => emptySisältäväOpiskeluoikeus)
                }
              >
                {t('Lisää')}
              </FlatButton>
            </ButtonGroup>
          )
        )}
      </KeyValueRow>
    </KeyValueTable>
  )
}

const SisältyyOpiskeluoikeuteenView = <T extends SisältäväOpiskeluoikeus>({
  value
}: CommonProps<FieldViewerProps<T | undefined, EmptyObject>>) => {
  return (
    <KeyValueTable>
      <KeyValueRow localizableLabel={'Oid'}>{value?.oid}</KeyValueRow>
      <KeyValueRow localizableLabel={'Oppilaitos'}>
        <OrganisaatioView value={value?.oppilaitos} />
      </KeyValueRow>
    </KeyValueTable>
  )
}

const SisältyyOpiskeluoikeuteenEdit = ({
  value,
  onChange
}: CommonProps<
  FieldEditorProps<SisältäväOpiskeluoikeus | undefined, EmptyObject>
>) => {
  return (
    <>
      <KeyValueTable>
        <KeyValueRow localizableLabel={'Oid'}>
          <TextEdit
            value={value?.oid}
            onChange={(oid) =>
              oid &&
              onChange({ ...emptySisältäväOpiskeluoikeus, ...value, oid })
            }
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel={'Oppilaitos'}>
          <OppilaitosSelect
            value={value?.oppilaitos as OrganisaatioHierarkia | undefined}
            onChange={(org) =>
              org &&
              onChange({
                ...emptySisältäväOpiskeluoikeus,
                ...value,
                oppilaitos: Oppilaitos({ oid: org.oid, nimi: org.nimi })
              })
            }
            orgTypes={['OPPILAITOS']}
          />
        </KeyValueRow>
      </KeyValueTable>
      <IconButton
        charCode={CHARCODE_REMOVE}
        label={t('Poista')}
        onClick={() => onChange(undefined)}
        size="input"
      />
    </>
  )
}
