import React from 'baret'
import Http from '../util/http'
import { modelData, modelLookup, pushModelValue } from '../editor/EditorModel'
import Dropdown from '../components/Dropdown'

export const CurriculumEditor = ({ model }) => {
  const curriculumP = fetchCurriculum().map((obs) => obs.map((k) => k.data))
  const curriculumModel = modelLookup(model, 'curriculum')
  const updateCurriculum = (curriculum) =>
    pushModelValue(curriculumModel, { data: curriculum })
  const automaticUpdatePossible = (curriculums) =>
    curriculums.length === 1 &&
    modelData(curriculumModel, 'koodiarvo') !== curriculums[0].koodiarvo

  curriculumP.onValue(
    (curriculums) =>
      model.context.edit &&
      automaticUpdatePossible(curriculums) &&
      updateCurriculum(curriculums[0])
  )

  return (
    <span>
      {curriculumP.map((curriculums) =>
        curriculums.length >= 1 && model.context.edit ? (
          <CurriculumDropdown
            options={curriculums}
            selected={modelData(curriculumModel)}
            onSelectionChanged={updateCurriculum}
          />
        ) : (
          <Curriculum model={curriculumModel} />
        )
      )}
    </span>
  )
}

const CurriculumDropdown = ({ options, onSelectionChanged, selected }) => (
  <Dropdown
    options={options}
    selected={selected}
    onSelectionChanged={onSelectionChanged}
    keyValue={(k) => k.koodiarvo}
    displayValue={(k) => k.koodiarvo}
  />
)

const Curriculum = ({ model }) => <span>{modelData(model, 'koodiarvo')}</span>

const fetchCurriculum = () =>
  Http.cachedGet(`/koski/api/editor/koodit/europeanschoolofhelsinkicurriculum`)
