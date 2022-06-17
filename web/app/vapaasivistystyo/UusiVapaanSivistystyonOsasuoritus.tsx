import React from "baret";
import Atom from "bacon.atom";
import {
  modelSet,
  modelSetTitle,
  modelSetValues,
  pushModel,
  modelLookup,
  modelData,
} from "../editor/EditorModel";
import { koodistoValues } from "../uusioppija/koodisto";
import KoodistoDropdown from "../koodisto/KoodistoDropdown";
import { t } from "../i18n/i18n";
import { koulutusModuuliprototypes } from "../suoritus/Koulutusmoduuli";
import { enumValueToKoodiviiteLens } from "../koodisto/koodistot";
import ModalDialog from "../editor/ModalDialog";
import Text from "../i18n/Text";
import { ift, notUndefined } from "../util/util";
import DropDown from "../components/Dropdown";
import { elementWithLoadingIndicator } from "../components/AjaxLoadingIndicator";

import {
  getOrganizationalPreferences,
  deleteOrganizationalPreference,
} from "../virkailija/organizationalPreferences";
import {
  EditorModel,
  EnumValue,
  ObjectModel,
  StringModel,
} from "../types/EditorModels";
import { ChangeBusContext, Contextualized } from "../types/EditorModelContext";
import { OsasuoritusEditorModel } from "../types/OsasuoritusEditorModel";
import {
  OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
} from "../types/VapaaSivistystyo";

export type UusiVapaanSivistystyonOsasuoritusProps = {
  suoritusPrototypes: OsasuoritusEditorModel[];
  setExpanded: any;
  suoritukset: ObjectModel[];
};

export const UusiVapaanSivistystyonOsasuoritus = ({
  suoritusPrototypes,
  setExpanded,
  suoritukset,
}: UusiVapaanSivistystyonOsasuoritusProps) => {
  const findSuoritus = (tyyppi: string) =>
    suoritukset.find((s) => s.value.classes.includes(tyyppi));
  const findSuoritusPrototyyppi = (tyyppi: string) =>
    suoritusPrototypes.find((s) => s.value.classes.includes(tyyppi));

  const osaamiskokonaisuus = findSuoritusPrototyyppi(
    "oppivelvollisillesuunnatunvapaansivistystyonosaamiskokonaisuudensuoritus"
  );
  const suuntautumisopinnot = findSuoritusPrototyyppi(
    "oppivelvollisillesuunnatunvapaansivistystyonvalinnaistensuuntautumisopintojensuoritus"
  );
  const muuallaSuoritettuOpinto = findSuoritusPrototyyppi(
    "muuallasuoritettuoppivelvollisillesuunnatunvapaansivistystyonopintojensuoritus"
  );
  const opintokokonaisuus = findSuoritusPrototyyppi(
    "oppivelvollisillesuunnatunvapaansivistystyonopintokokonaisuudensuoritus"
  );

  const kotoOsaAlueKieliOpinnot =
    !findSuoritus(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus"
    ) &&
    findSuoritusPrototyyppi(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus"
    );
  const kotoOsaAlueTyöelämäJaYhteiskuntataidot =
    !findSuoritus(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenopintojensuoritus"
    ) &&
    findSuoritusPrototyyppi(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenopintojensuoritus"
    );
  const kotoOsaAlueOhjaus =
    !findSuoritus(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus"
    ) &&
    findSuoritusPrototyyppi(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus"
    );
  const kotoOsaAlueVapaavalintaiset =
    !findSuoritus(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojensuoritus"
    ) &&
    findSuoritusPrototyyppi(
      "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojensuoritus"
    );

  const kotoTyöelämäJaYhteiskuntataidot = findSuoritusPrototyyppi(
    "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataidot"
  );
  const kotoTyöelämäJaYhteiskuntataidotTyöelämäjakso = findSuoritusPrototyyppi(
    "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajakso"
  );
  const kotoValinnaisetOpinnot = findSuoritusPrototyyppi(
    "vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus"
  );

  const lukutaitokoulutuksenKokonaisuus = findSuoritusPrototyyppi(
    "vapaansivistystyonlukutaitokoulutuksenkokonaisuudensuoritus"
  );

  const vapaatavoitteisenOsasuoritus = findSuoritusPrototyyppi(
    "vapaansivistystyonvapaatavoitteisenkoulutuksenosasuorituksensuoritus"
  );

  return (
    <>
      {osaamiskokonaisuus && (
        <LisääKoodistosta
          koodistoUri={"vstosaamiskokonaisuus"}
          suoritusPrototype={osaamiskokonaisuus}
          className={"vst-osaamiskokonaisuus"}
          selectionText={"Lisää osaamiskokonaisuus"}
          setExpanded={setExpanded}
        />
      )}
      {suuntautumisopinnot && (
        <LisääKoodistosta
          koodistoUri={"vstmuutopinnot"}
          suoritusPrototype={suuntautumisopinnot}
          className={"vst-suuntautumisopinnot"}
          selectionText={"Lisää suuntautumisopinto"}
          setExpanded={setExpanded}
        />
      )}
      {muuallaSuoritettuOpinto && (
        <LisääKoodistosta
          koodistoUri={"vstmuuallasuoritetutopinnot"}
          suoritusPrototype={muuallaSuoritettuOpinto}
          className={"vst-muutopinnot"}
          selectionText={"Lisää muualla suoritettu opinto"}
          setExpanded={setExpanded}
        />
      )}
      {opintokokonaisuus && (
        <LisääPaikallinen
          suoritusPrototype={opintokokonaisuus}
          setExpanded={setExpanded}
          lisääText={"Lisää paikallinen opintokokonaisuus"}
          lisääTitle={"Paikallisen opintokokonaisuuden lisäys"}
        />
      )}
      {kotoTyöelämäJaYhteiskuntataidotTyöelämäjakso && (
        <LisääPaikallinen
          suoritusPrototype={kotoTyöelämäJaYhteiskuntataidotTyöelämäjakso}
          setExpanded={setExpanded}
          lisääText={"Lisää työelämäjakso"}
          lisääTitle={"Työelämäjakson lisäys"}
        />
      )}
      {kotoTyöelämäJaYhteiskuntataidot && (
        <LisääPaikallinen
          suoritusPrototype={kotoTyöelämäJaYhteiskuntataidot}
          setExpanded={setExpanded}
          lisääText={"Lisää työelämä- ja yhteiskuntataidon opintokokonaisuus"}
          lisääTitle={
            "Työelämä- ja yhteiskuntataidon opintokokonaisuuden lisäys"
          }
        />
      )}
      {kotoValinnaisetOpinnot && (
        <LisääPaikallinen
          suoritusPrototype={kotoValinnaisetOpinnot}
          setExpanded={setExpanded}
          lisääText={"Lisää valinnaiset"}
          lisääTitle={"Valinnaisen opintosuorituksen lisäys"}
        />
      )}
      {kotoOsaAlueKieliOpinnot && (
        <LisääOsaAlue
          suoritusPrototype={kotoOsaAlueKieliOpinnot}
          selectionText={
            "Lisää suomen/ruotsin kielen ja viestintätaitojen osa-alue"
          }
          setExpanded={setExpanded}
        />
      )}
      {kotoOsaAlueTyöelämäJaYhteiskuntataidot && (
        <LisääOsaAlue
          suoritusPrototype={kotoOsaAlueTyöelämäJaYhteiskuntataidot}
          selectionText={
            "Lisää työelämän ja yhteiskuntataitojen opintojen osa-alue"
          }
          setExpanded={setExpanded}
        />
      )}
      {kotoOsaAlueOhjaus && (
        <LisääOsaAlue
          suoritusPrototype={kotoOsaAlueOhjaus}
          selectionText={"Lisää kotoutumiskoulutuksen ohjauksen osa-alue"}
          setExpanded={setExpanded}
        />
      )}
      {kotoOsaAlueVapaavalintaiset && (
        <LisääOsaAlue
          suoritusPrototype={kotoOsaAlueVapaavalintaiset}
          selectionText={"Lisää valinnaisten opintojen osa-alue"}
          setExpanded={setExpanded}
        />
      )}
      {lukutaitokoulutuksenKokonaisuus && (
        <LisääKoodistosta
          koodistoUri={"vstlukutaitokoulutuksenkokonaisuus"}
          suoritusPrototype={lukutaitokoulutuksenKokonaisuus}
          className={"vst-lukutaitokoulutuksenkokonaisuudensuoritus"}
          selectionText={"Lisää kokonaisuus"}
          setExpanded={setExpanded}
        />
      )}
      {vapaatavoitteisenOsasuoritus && (
        <LisääPaikallinen
          suoritusPrototype={vapaatavoitteisenOsasuoritus}
          setExpanded={setExpanded}
          lisääText={"Lisää osasuoritus"}
          lisääTitle={"Osasuorituksen lisäys"}
        />
      )}
    </>
  );
};

type LisääKoodistostaProps = {
  suoritusPrototype: ObjectModel & Contextualized<ChangeBusContext>;
  koodistoUri: string;
  className: string;
  selectionText: string;
  setExpanded: (suoritus: EditorModel) => (expanded: boolean) => void;
};

const LisääKoodistosta = ({
  suoritusPrototype,
  koodistoUri,
  className,
  selectionText,
  setExpanded,
}: LisääKoodistostaProps) => {
  const selectedAtom = Atom<EnumValue<string> | undefined>();
  const koulutusmoduuliPrototype =
    koulutusModuuliprototypes(suoritusPrototype)[0];

  selectedAtom.filter(notUndefined).onValue((newItem) => {
    const koulutusmoduuli = modelSetTitle(
      modelSetValues(koulutusmoduuliPrototype, { tunniste: newItem }),
      newItem.title
    );
    const suoritus = modelSet(
      suoritusPrototype,
      koulutusmoduuli,
      "koulutusmoduuli"
    );
    pushModel(suoritus);
    setExpanded(suoritus)(true);
    selectedAtom.set(undefined);
  });

  return (
    <div className={"lisaa-uusi-suoritus " + className}>
      {/* @ts-expect-error KoodistoDropdown */}
      <KoodistoDropdown
        className={className}
        options={koodistoValues(koodistoUri)}
        selected={selectedAtom.view(enumValueToKoodiviiteLens)}
        selectionText={t(selectionText)}
      />
    </div>
  );
};

type LisääOsaAlueProps = {
  suoritusPrototype: EditorModel & Contextualized<ChangeBusContext>;
  selectionText: string;
  setExpanded: (suoritus: EditorModel) => (expanded: boolean) => void;
};

const LisääOsaAlue = ({
  suoritusPrototype,
  selectionText,
  setExpanded,
}: LisääOsaAlueProps) => {
  const koulutusmoduuliPrototype =
    koulutusModuuliprototypes(suoritusPrototype)[0];

  const addNewOsaAlue = () => {
    const tunniste = (
      modelLookup(koulutusmoduuliPrototype, "tunniste") as StringModel
    ).value;
    const koulutusmoduuli = modelSetTitle(
      modelSetValues(koulutusmoduuliPrototype, { tunniste }),
      tunniste.title || ""
    );
    const suoritus = modelSet(
      suoritusPrototype,
      koulutusmoduuli,
      "koulutusmoduuli"
    );
    pushModel(suoritus);
    setExpanded(suoritus)(true);
  };

  return (
    <div className={"lisaa-uusi-suoritus"}>
      <span className="lisaa-osa-alueen-suoritus">
        <a className="add-link" onClick={() => addNewOsaAlue()}>
          {/* @ts-expect-error Text */}
          <Text name={selectionText} />
        </a>
      </span>
    </div>
  );
};

type LisääPaikallinenProps = {
  suoritusPrototype: OsasuoritusEditorModel;
  setExpanded: (suoritus: EditorModel) => (expanded: boolean) => void;
  lisääText: string;
  lisääTitle: string;
};

const LisääPaikallinen = ({
  suoritusPrototype,
  setExpanded,
  lisääText,
  lisääTitle,
}: LisääPaikallinenProps) => {
  type TallennettuSuoritus =
    | OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus;

  const showModal = Atom(false);
  const inputState = Atom("");
  const validP = inputState.map(Boolean);

  const closeModal = () => showModal.set(false);
  const updateInputState = (event: React.ChangeEvent<HTMLInputElement>) =>
    inputState.set(event.target.value);

  const koulutusmoduuliPrototype =
    koulutusModuuliprototypes(suoritusPrototype)[0] as OsasuoritusEditorModel;

  const addNewSuoritus = (storedSuoritus: TallennettuSuoritus) => {
    console.log("addNewSuoritus - storedSuoritus", storedSuoritus);
    const input = inputState.get();
    const updateValues = {
      "kuvaus.fi": { data: storedSuoritus ? storedSuoritus.kuvaus.fi : input },
      "tunniste.nimi.fi": {
        data: storedSuoritus ? storedSuoritus.tunniste.nimi.fi : input,
      },
      "tunniste.koodiarvo": {
        data: storedSuoritus ? storedSuoritus.tunniste.koodiarvo : input,
      },
    };
    const koulutusmoduuli = modelSetTitle(
      modelSetValues(koulutusmoduuliPrototype, updateValues),
      storedSuoritus ? storedSuoritus.tunniste.nimi.fi : input
    );
    const suoritus = modelSet(
      suoritusPrototype,
      koulutusmoduuli,
      "koulutusmoduuli"
    );
    pushModel(suoritus);
    setExpanded(suoritus)(true);
    showModal.set(false);
  };

  const x = modelData(suoritusPrototype.context.opiskeluoikeus, "suoritukset");

  const päätasonSuoritus = modelData(
    suoritusPrototype.context.opiskeluoikeus,
    "suoritukset"
  )[0];

  const organisaatioOid = päätasonSuoritus.toimipiste.oid;
  const key = modelLookup(suoritusPrototype, "koulutusmoduuli")?.value
    .classes[0];

  const setOptions = (suoritukset: EditorModel[]) => {
    const tallennetutSuoritukset = suoritukset.map((suoritus) => {
      return {
        kuvaus: modelData(suoritus, "kuvaus"),
        tunniste: modelData(suoritus, "tunniste"),
      };
    });
    options.set(tallennetutSuoritukset);
  };

  const options = Atom<TallennettuSuoritus[]>([]);
  getOrganizationalPreferences(organisaatioOid, key).onValue(
    (value: EditorModel[]) => {
      setOptions(value);
    }
  );

  const newOsasuoritus = {
    kuvaus: { fi: "" },
    tunniste: { nimi: { fi: "" }, koodiarvo: "" },
    uusi: true,
  };

  const poistaPaikallinenOsasuoritus = (osasuoritus: TallennettuSuoritus) => {
    const avain = osasuoritus.tunniste.koodiarvo;
    const tyyppi = koulutusmoduuliPrototype.value.classes[0];
    deleteOrganizationalPreference(organisaatioOid, tyyppi, avain).onValue(
      setOptions
    );
  };

  return (
    <div className={"lisaa-uusi-suoritus paikallinen"}>
      <span className="lisaa-paikallinen-suoritus">
        {elementWithLoadingIndicator(
          options.map(".length").map(
            // @ts-expect-error DropDown
            <DropDown
              options={options}
              keyValue={(option) =>
                option.uusi ? "uusi" : "lisää " + option.tunniste.koodiarvo
              }
              displayValue={(option) =>
                option.uusi ? "Lisää uusi" : option.tunniste.nimi.fi
              }
              selectionText={lisääText}
              isRemovable={() => true}
              newItem={newOsasuoritus}
              removeText={t(
                "Poista osasuoritus. Poistaminen ei vaikuta olemassa oleviin suorituksiin."
              )}
              // @ts-expect-error DropDown
              onSelectionChanged={(option) =>
                option.uusi ? showModal.set(true) : addNewSuoritus(option)
              }
              onRemoval={poistaPaikallinenOsasuoritus}
            />
          )
        )}
        {ift(
          showModal, // @ts-expect-error ModalDialog
          <ModalDialog
            className="lisaa-paikallinen-vst-suoritus-modal"
            onDismiss={closeModal}
            onSubmit={addNewSuoritus}
            okTextKey={lisääText}
            validP={validP}
          >
            <h2>
              {/* @ts-expect-error Text */}
              <Text name={lisääTitle} />
            </h2>
            <label>
              {/* @ts-expect-error Text */}
              <Text name={"Opintokokonaisuuden nimi"} />
              <input
                className="paikallinen-koulutusmoduuli-nimi"
                type="text"
                autoFocus={true}
                onChange={updateInputState}
              />
            </label>
          </ModalDialog>
        )}
      </span>
    </div>
  );
};
