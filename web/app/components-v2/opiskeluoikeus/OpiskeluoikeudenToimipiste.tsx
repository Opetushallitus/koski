import React, { useEffect, useMemo, useCallback, useState } from 'react'
// @ts-expect-error
import { debounce } from 'lodash'
import { useOrganisaatioHierarkia } from '../../appstate/organisaatioHierarkia'
import { t } from '../../i18n/i18n'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  isKoulutustoimija,
  Koulutustoimija
} from '../../types/fi/oph/koski/schema/Koulutustoimija'
import {
  isOidOrganisaatio,
  OidOrganisaatio
} from '../../types/fi/oph/koski/schema/OidOrganisaatio'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import {
  isOppilaitos,
  Oppilaitos
} from '../../types/fi/oph/koski/schema/Oppilaitos'
import {
  isToimipiste,
  Toimipiste
} from '../../types/fi/oph/koski/schema/Toimipiste'
import { assertNever } from '../../util/selfcare'
import { common, CommonProps, testId } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { useDialog } from '../containers/Dialog'

export type Suorituskielikoodiviite = Koodistokoodiviite<'kieli'>

type KoskiToimipiste = Opiskeluoikeus['suoritukset'][number]['toimipiste']
export type ToimipisteViewProps = CommonProps<
  FieldViewerProps<KoskiToimipiste, {}>
>

export const ToimipisteView: React.FC<ToimipisteViewProps> = (props) => {
  const { value } = props
  if (!value) {
    return (
      <div>
        <strong>{'-'}</strong>
      </div>
    )
  }
  if (isKoulutustoimija(value)) {
    return (
      <div
        {...common(props, ['ToimipisteView'])}
        {...testId(props, 'koulutustoimija')}
      >
        {t(value.nimi)}
      </div>
    )
  }
  if (isOidOrganisaatio(value)) {
    return (
      <div
        {...common(props, ['ToimipisteView'])}
        {...testId(props, 'oidOrganisaatio')}
      >
        {t(value.nimi)}
      </div>
    )
  }
  if (isOppilaitos(value)) {
    return (
      <div
        {...common(props, ['ToimipisteView'])}
        {...testId(props, 'oppilaitos')}
      >
        {t(value.nimi)}
      </div>
    )
  }
  if (isToimipiste(value)) {
    return (
      <div
        {...common(props, ['ToimipisteView'])}
        {...testId(props, 'toimipiste')}
      >
        {t(value.nimi)}
      </div>
    )
  }

  return assertNever(value)
}

export type ToimipisteEditProps = CommonProps<
  FieldEditorProps<
    KoskiToimipiste,
    { onChangeToimipiste?: (data: KoskiToimipiste) => void }
  >
>

const OrgHierarkia: React.FC<{
  hierarkia: OrganisaatioHierarkia
  onSelect: (e: KoskiToimipiste) => void
}> = (props) => {
  const hierarkia = props.hierarkia
  const { onSelect } = props
  return (
    <li>
      <a
        href="#"
        onClick={(e) => {
          e.preventDefault()
          if (hierarkia.organisaatiotyypit.includes('OPPILAITOS')) {
            onSelect(
              Oppilaitos({
                oid: props.hierarkia.oid,
                // @ts-expect-error
                oppilaitosnumero: props.hierarkia.oppilaitosnumero,
                nimi: props.hierarkia.nimi,
                // @ts-expect-error
                kotipaikka: props.hierarkia.kotipaikka
              })
            )
          } else if (hierarkia.organisaatiotyypit.includes('TOIMIPISTE')) {
            onSelect(
              Toimipiste({
                oid: props.hierarkia.oid,
                nimi: hierarkia.nimi
              })
            )
          } else if (hierarkia.organisaatiotyypit.includes('KOULUTUSTOIMIJA')) {
            onSelect(
              Koulutustoimija({
                oid: props.hierarkia.oid,
                nimi: hierarkia.nimi
              })
            )
          } else {
            throw new Error(
              'Unknown error - non-exhaustive checks for OrganisaatioHierarkia type'
            )
          }
        }}
      >
        {t(props.hierarkia.nimi)}
      </a>
      {props.hierarkia.children.length > 0 &&
        props.hierarkia.children.map((oh, i) => (
          <ul key={i}>
            <OrgHierarkia hierarkia={oh} onSelect={props.onSelect} />
          </ul>
        ))}
    </li>
  )
}

export const ToimipisteEdit: React.FC<ToimipisteEditProps> = (props) => {
  /**
   * - hakupalkki
   * - hakutulokset filtteröitynä
   * - knu hakutulosta painaa --> valitsee toimipisteen
   */
  const [searchQuery, setSearchQuery] = useState('')
  const [debounceQuery, setDebounceQuery] = useState('')
  const hierarkia = useOrganisaatioHierarkia(debounceQuery)

  const { closeDialog, openDialog, Dialog } = useDialog(
    'OpiskeluoikeudenToimipiste'
  )

  const { onChangeToimipiste } = props

  const debouncedSearchQuery = useMemo(
    () =>
      debounce((input: string) => {
        setDebounceQuery(input)
      }, 200),
    []
  )

  const onSelectData = useCallback(
    (data: KoskiToimipiste) => {
      const { $class } = data
      switch ($class) {
        case Koulutustoimija.className:
        case OidOrganisaatio.className:
        case Oppilaitos.className:
        case Toimipiste.className:
          closeDialog()
          setSearchQuery('')
          setDebounceQuery('')
          if (onChangeToimipiste !== undefined) {
            onChangeToimipiste(data)
          }
          break
        default:
          return assertNever($class)
      }
    },
    [closeDialog, onChangeToimipiste]
  )

  useEffect(() => {
    debouncedSearchQuery(searchQuery)
  }, [debouncedSearchQuery, searchQuery])

  return (
    <>
      <button
        className="OpiskeluoikeudenToimipiste-Edit-View"
        onClick={(e) => {
          e.preventDefault()
          openDialog()
        }}
      >
        <ToimipisteView value={props.value} />
      </button>
      <Dialog data-testid="toimipiste-dialog">
        <h3>{t('toimipiste:hae_oppilaitosta_tai_toimipistetta')}</h3>
        <input
          type="text"
          autoFocus
          size={100}
          value={searchQuery}
          placeholder={t('toimipiste:oppilaitos_tai_toimipiste')}
          onChange={(e) => {
            e.preventDefault()
            setSearchQuery(e.target.value)
          }}
        />
        <div className="OpiskeluoikeudenToimipiste__Dialog__Inner">
          {hierarkia.length === 0 && debounceQuery.length > 0 && (
            <strong>{t('toimipiste:ei_hakutuloksia')}</strong>
          )}
          <ul>
            {hierarkia.map((h, i) => (
              <OrgHierarkia key={i} hierarkia={h} onSelect={onSelectData} />
            ))}
          </ul>
        </div>
      </Dialog>
    </>
  )
}