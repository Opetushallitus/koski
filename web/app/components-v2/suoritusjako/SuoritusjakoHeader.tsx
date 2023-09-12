import React from 'baret'
import Text from '../../i18n/Text'
import { modelData, modelItems, modelLookup } from '../../editor/EditorModel'
import { Varoitukset } from '../../util/Varoitukset'
import { HeaderName } from '../../omattiedot/header/HeaderName'
import { EditorModel } from '../../types/EditorModels'
import { CommonProps } from '../CommonProps'

export type SuoritusjakoHeaderProps = CommonProps<{
  oppija: EditorModel
  secret: string
  testId?: string
}>

export const SuoritusjakoHeader: React.FC<SuoritusjakoHeaderProps> = (
  props
) => {
  const henkilö = modelLookup(props.oppija, 'henkilö')
  const varoitukset = modelItems(props.oppija, 'varoitukset').map(modelData)
  return (
    <header>
      <Varoitukset varoitukset={varoitukset} />
      <h1 className="header__heading">
        {/* @ts-expect-error Text */}
        <Text name="Opinnot" />
      </h1>
      <div className="header__bottom-row">
        <HeaderName henkilö={henkilö} />
      </div>
      <a
        className="text-button-small"
        target="_blank"
        href={`/koski/api/opinnot/${props.secret}`}
        rel="noopener noreferrer"
      >
        {/* @ts-expect-error Text */}
        <Text
          data-testid={`${props.testId ? props.testId : ''}__koneluettava-link`}
          name="Tiedot koneluettavassa muodossa"
        />
      </a>
    </header>
  )
}
