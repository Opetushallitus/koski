import React from "baret";
import { modelLookup } from "../editor/EditorModel";
import { PropertiesEditor } from "../editor/PropertiesEditor";
import { pushRemoval } from "../editor/EditorModel";
import { suoritusProperties } from "../suoritus/SuoritustaulukkoCommon";
import { TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko } from "./TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko";
import { ObjectModel } from "../types/EditorModels";
import { ChangeBusContext, Contextualized } from "../types/EditorModelContext";

export type TuvaOsasuoritusModel = ObjectModel &
  Contextualized<ChangeBusContext>;

export type TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditorProps = {
  model: TuvaOsasuoritusModel;
  onExpand: () => void;
  expanded: boolean;
  nestedLevel: number;
  columns: any[];
};

export const TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditor = ({
  model,
  onExpand,
  expanded,
  nestedLevel,
  columns,
}: TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditorProps) => {
  const editableProperties = suoritusProperties(model).filter(
    (p) => p.key !== "osasuoritukset"
  );
  const osasuoritukset = modelLookup(model, "osasuoritukset");

  const laajuusReadOnly =
    model &&
    model.value.classes.includes(
      "tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenosansuoritus"
    );

  return (
    <tbody className={"tuva-osasuoritus tuva-osasuoritus-" + nestedLevel}>
      <tr className={"tuva-osasuoritusrivi-" + nestedLevel}>
        {columns.map((column) =>
          column.renderData({
            model,
            expanded,
            onExpand,
            showTila: true,
            hasProperties: true,
            disabled: laajuusReadOnly,
          })
        )}
        {model.context.edit && (
          <td className="remove">
            <a className="remove-value" onClick={() => pushRemoval(model)} />
          </td>
        )}
      </tr>
      {expanded && editableProperties.length > 0 && (
        <tr className="details" key="details">
          <td colSpan={4}>
            <PropertiesEditor model={model} properties={editableProperties} />
          </td>
        </tr>
      )}
      {expanded && (
        <tr className="osasuoritukset">
          <td colSpan={4}>
            <TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko
              parentSuoritus={model}
              suorituksetModel={osasuoritukset}
              nestedLevel={nestedLevel}
            />
          </td>
        </tr>
      )}
    </tbody>
  );
};
