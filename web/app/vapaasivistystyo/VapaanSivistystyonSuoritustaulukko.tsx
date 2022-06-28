import React, { ReactNode } from "baret";
import * as R from "ramda";
import { modelData, modelItems } from "../editor/EditorModel";
import { accumulateExpandedState } from "../editor/ExpandableItems";
import Text from "../i18n/Text";
import { t } from "../i18n/i18n";
import { tutkinnonOsaPrototypes } from "../ammatillinen/TutkinnonOsa";
import { UusiVapaanSivistystyonOsasuoritus } from "./UusiVapaanSivistystyonOsasuoritus";
import { VapaanSivistystyonOsasuoritusEditor } from "./VapaanSivistystyonOsasuoritusEditor";
import {
  ArvosanaColumn,
  ExpandAllRows,
  getLaajuusYksikkö,
  LaajuusColumn,
  SuoritusColumn,
  TaitotasoColumn,
} from "../suoritus/SuoritustaulukkoCommon";
import { numberToString } from "../util/format";
import { EditorModel, ObjectModel } from "../types/EditorModels";
import { Contextualized } from "../types/EditorModelContext";
import { OsasuoritusEditorModel } from "../types/OsasuoritusEditorModel";

const MAX_NESTED_LEVEL_VAPAATAVOITTEINEN = 3;
const MAX_NESTED_LEVEL_MUUT = 2;

export type VapaanSivistystyonSuoritustaulukkoProps = {
  parentSuoritus: OsasuoritusEditorModel;
  suorituksetModel?: OsasuoritusEditorModel;
  nestedLevel?: number;
};

type VSTColumn =
  | SuoritusColumn
  | LaajuusColumn
  | ArvosanaColumn
  | TaitotasoColumn;

export class VapaanSivistystyonSuoritustaulukko extends React.Component<VapaanSivistystyonSuoritustaulukkoProps> {
  render() {
    const { parentSuoritus, suorituksetModel, nestedLevel = 0 } = this.props;
    const context = parentSuoritus.context;
    const suoritukset = modelItems(suorituksetModel) || [];
    const parentOneOf = (...classes: string[]) =>
      classes.some((c) => parentSuoritus.value.classes.includes(c));
    const maxNestedLevel = parentOneOf(
      "vapaansivistystyonvapaatavoitteisenkoulutuksenosasuorituksensuoritus"
    )
      ? MAX_NESTED_LEVEL_VAPAATAVOITTEINEN
      : MAX_NESTED_LEVEL_MUUT;

    if (
      (suoritukset.length === 0 && !context.edit) ||
      nestedLevel >= maxNestedLevel ||
      suorituksetModel === undefined
    ) {
      return null;
    }

    const { isExpandedP, allExpandedP, toggleExpandAll, setExpanded } =
      accumulateExpandedState({ suoritukset, component: this });

    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel);
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProtos[0]);
    const osaAlueTitle = () => {
      if (
        parentOneOf(
          "oppivelvollisillesuunnattuvapaansivistystyonkoulutuksensuoritus"
        )
      ) {
        return t("Osaamiskokonaisuus");
      } else if (
        parentOneOf("vapaansivistystyonvapaatavoitteisenkoulutuksensuoritus")
      ) {
        return t("Koulutus");
      } else {
        return t("Osa-alue");
      }
    };
    const suoritusTitle =
      nestedLevel === 0 ? osaAlueTitle() : t("Opintokokonaisuus");

    const columns = [
      SuoritusColumn,
      LaajuusColumn,
      ArvosanaColumn,
      TaitotasoColumn,
    ].filter((column) =>
      column.shouldShow({
        parentSuoritus,
        suoritukset,
        suorituksetModel,
        context,
      })
    );

    const canExpand = nestedLevel < maxNestedLevel - 1;

    return (
      <div className="suoritus-taulukko">
        <table>
          {nestedLevel === 0 && (
            <ExpandAllRows
              allExpandedP={allExpandedP}
              // @ts-expect-error
              toggleExpandAll={toggleExpandAll}
            />
          )}
          <tbody className="taulukko-headers">
            <tr>
              {columns.map(
                (
                  column // @ts-expect-error SuoritustaulukkoCommon
                ) => column.renderHeader({ laajuusYksikkö, suoritusTitle })
              )}
            </tr>
          </tbody>
          {suoritukset.map((suoritus, i) => (
            <VapaanSivistystyonOsasuoritusEditor
              baret-lift
              key={i}
              model={suoritus}
              expanded={isExpandedP(suoritus)}
              onExpand={canExpand ? setExpanded(suoritus) : undefined}
              columns={columns}
              nestedLevel={nestedLevel + 1}
            />
          ))}
          {context.edit && (
            <SingleColumnRowTable colSpan={4}>
              <UusiVapaanSivistystyonOsasuoritus
                // @ts-expect-error TutkinnonOsa
                suoritusPrototypes={
                  suoritusProtos as Array<ObjectModel & Contextualized>
                }
                setExpanded={setExpanded}
                suoritukset={suoritukset}
              />
            </SingleColumnRowTable>
          )}
          {nestedLevel === 0 && (
            <SingleColumnRowTable className={"yhteislaajuus"}>
              <YhteensäSuoritettu
                suoritukset={suoritukset}
                // @ts-expect-error YhteensäSuoritettu
                laajuusYksikkö={laajuusYksikkö}
              />
            </SingleColumnRowTable>
          )}
        </table>
      </div>
    );
  }
}

type YhteensäSuoritettuProps = {
  suoritukset: EditorModel[];
  laajuusYksikkö: string;
};

const YhteensäSuoritettu = ({
  suoritukset,
  laajuusYksikkö,
}: YhteensäSuoritettuProps) => {
  const laajuudetYhteensä = R.sum(
    R.map(
      (item) => modelData(item, "koulutusmoduuli.laajuus.arvo") || 0,
      suoritukset
    )
  );
  return (
    <div>
      {/* @ts-expect-error TODO: Tyypitä Text */}
      <Text name="Yhteensä" />{" "}
      <span className="laajuudet-yhteensä">
        {numberToString(laajuudetYhteensä)}
      </span>{" "}
      {laajuusYksikkö}
    </div>
  );
};

type SingleColumnRowTableProps = {
  children?: ReactNode;
  className?: string;
  colSpan?: number;
};

const SingleColumnRowTable = ({
  children,
  className = "",
  colSpan = 1,
}: SingleColumnRowTableProps) => (
  <tbody className={className}>
    <tr>
      <td colSpan={colSpan}>{children}</td>
    </tr>
  </tbody>
);
