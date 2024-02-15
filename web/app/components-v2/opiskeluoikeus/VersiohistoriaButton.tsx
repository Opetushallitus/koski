import React, { useCallback, useMemo, useRef, useState } from 'react'
import {
  createLocalThenApiCache,
  isSuccess,
  useApiWithParams
} from '../../api-fetch'
import { useVersionumero } from '../../appstate/useSearchParam'
import { TestIdLayer } from '../../appstate/useTestId'
import { ISO2FinnishDateTime } from '../../date/date'
import { t } from '../../i18n/i18n'
import { last } from '../../util/fp/arrays'
import { fetchVersiohistoria } from '../../util/koskiApi'
import { currentQueryWith } from '../../util/url'
import { CommonProps, cx } from '../CommonProps'
import { PositionalPopup } from '../containers/PositionalPopup'
import { FlatButton } from '../controls/FlatButton'
import { LinkButton } from '../controls/LinkButton'

export type VersiohistoriaButtonProps = CommonProps<{
  opiskeluoikeusOid: string
}>

export const VersiohistoriaButton: React.FC<VersiohistoriaButtonProps> = (
  props
) => {
  const buttonRef = useRef(null)
  const [versiohistoriaVisible, setVersiohistoriaVisible] = useState(false)
  const toggleList = useCallback(
    () => setVersiohistoriaVisible(!versiohistoriaVisible),
    [versiohistoriaVisible]
  )
  const hideList = useCallback(() => setVersiohistoriaVisible(false), [])
  const currentVersion = useVersionumero()

  return (
    <TestIdLayer id="versiohistoria">
      <span className="VersiohistoriaButton" ref={buttonRef}>
        <FlatButton
          onClick={toggleList}
          aria-haspopup="menu"
          aria-expanded={versiohistoriaVisible}
          testId="button"
        >
          {currentVersion
            ? `${t('Versionumero')}: v${currentVersion}`
            : t('Versiohistoria')}
        </FlatButton>
        <PositionalPopup
          align="right"
          onDismiss={hideList}
          open={versiohistoriaVisible}
          parentRef={buttonRef}
        >
          <VersiohistoriaList
            opiskeluoikeusOid={props.opiskeluoikeusOid}
            open={versiohistoriaVisible}
          />
        </PositionalPopup>
      </span>
    </TestIdLayer>
  )
}

type VersiohistoriaListProps = CommonProps<{
  opiskeluoikeusOid: string
  open: boolean
}>

const versiolistaCache = createLocalThenApiCache(fetchVersiohistoria)

const VersiohistoriaList: React.FC<VersiohistoriaListProps> = (props) => {
  const historia = useApiWithParams(
    fetchVersiohistoria,
    props.open ? [props.opiskeluoikeusOid] : undefined,
    versiolistaCache
  )

  const versioParam = useVersionumero()

  const currentVersion = useMemo(() => {
    return versioParam
      ? parseInt(versioParam)
      : isSuccess(historia)
        ? last(historia.data)?.versionumero
        : undefined
  }, [historia, versioParam])

  return isSuccess(historia) ? (
    <TestIdLayer id="list">
      <ul className="VersiohistoriaList" role="navigation">
        {historia.data.map((versio) => (
          <li
            key={versio.versionumero}
            className={cx(
              'VersiohistoriaList__item',
              currentVersion === versio.versionumero &&
                'VersiohistoriaList__item--current'
            )}
          >
            <LinkButton
              href={currentQueryWith({
                opiskeluoikeus: props.opiskeluoikeusOid,
                versionumero: versio.versionumero
              })}
              testId={versio.versionumero}
            >
              {`v${versio.versionumero}`}{' '}
              {ISO2FinnishDateTime(versio.aikaleima)}
            </LinkButton>
          </li>
        ))}
        {versioParam && (
          <li className="VersiohistoriaList__item">
            <PoistuVersiohistoriastaButton
              opiskeluoikeusOid={props.opiskeluoikeusOid}
            />
          </li>
        )}
      </ul>
    </TestIdLayer>
  ) : null
}

export type PoistuVersiohistoriastaButtonProps = {
  opiskeluoikeusOid?: string
}

export const PoistuVersiohistoriastaButton = (
  props: PoistuVersiohistoriastaButtonProps
) => (
  <LinkButton
    href={currentQueryWith({
      opiskeluoikeus: props.opiskeluoikeusOid || null,
      versionumero: null
    })}
  >
    {t('Poistu versiohistoriasta')}
  </LinkButton>
)
