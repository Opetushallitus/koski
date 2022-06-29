import Bacon from "baconjs";
import * as R from "ramda";
import {
  EditorModel,
  isDateModel,
  isObjectModel,
  ObjectModel,
} from "../types/EditorModels";

// @ts-expect-error
export const doActionWhileMounted = (stream, action) =>
  stream.doAction(action).map(null).toProperty().startWith(null);

export const parseBool = (b: any, defaultValue: boolean): boolean => {
  if (typeof b === "string") {
    return b === "true";
  }
  if (b === undefined) {
    b = defaultValue;
  }
  return b;
};

const isObs = <T>(x: any): x is Bacon.Observable<T> =>
  x instanceof Bacon.Observable;

export const toObservable = <T>(x: T | Bacon.Observable<T>) =>
  isObs(x) ? x : Bacon.constant(x);

export const ift = <T>(obs: Bacon.Observable<boolean>, value: T) =>
  toObservable(obs).map((show) => (show ? value : null));

export const is = (model?: EditorModel) => {
  const instanceOf = (className: string) =>
    (model as any)?.value?.classes?.includes(className) || false;
  return { instanceOf };
};

export const scrollElementBottomVisible = (elem: HTMLElement) => {
  if (elem) {
    let elementTopPositionOnScreen = elem.getBoundingClientRect().y;
    let elementBottomPositionOnScreen =
      elementTopPositionOnScreen + elem.getBoundingClientRect().height;
    let marginBottom = window.innerHeight - elementBottomPositionOnScreen - 20;
    let marginTop = elementTopPositionOnScreen - 20;
    let toScroll = -marginBottom;

    if (toScroll > 0 && toScroll <= marginTop) {
      window.scrollBy(0, toScroll);
    }
  }
};

export const flatMapArray = <T, S>(a: T[], f: (t: T) => S[]): S[] =>
  R.unnest(a.map(f));

// FIXME: because element.focus() option preventScroll is still experimental API, it can't really be used
// This is kind of a dirty solution to keep the page from jumping on focus, but at the time of writing at least
// Safari doesn't support focus options. Should be replaced with the use of focusOptions when properly supported by browsers
export const focusWithoutScrolling = (elem: HTMLElement) => {
  if (!elem) return;
  const { scrollX, scrollY } = window;
  elem.focus();
  window.scrollTo(scrollX, scrollY);
};

export const findPropertyModel = (
  model: ObjectModel,
  propertyKey: string
): EditorModel | undefined =>
  model.value.properties.find((p) => p.key === propertyKey)?.model;

export const getBirthdayFromEditorRes = (
  editorResponse: ObjectModel
): Date | undefined => {
  const henkilö = findPropertyModel(editorResponse, "henkilö");
  const syntymäaika =
    henkilö &&
    isObjectModel(henkilö) &&
    findPropertyModel(henkilö, "syntymäaika");
  return syntymäaika && isDateModel(syntymäaika)
    ? syntymäaika.value.data
    : undefined;
};

export const notUndefined = <T>(t?: T): t is T => t !== undefined;

export const debugFn =
  <A extends any[], T>(
    name: string,
    debuggedFn: (...a: A) => T,
    guard: (input: A, output: T) => boolean = () => true
  ) =>
  (...args: A) => {
    const result = debuggedFn(...args);
    if (guard(args, result)) {
      console.log(`${name}(`, args, "):", result);
    }
    return result;
  };

export const debugObservables = (observables: Record<string, Bacon.Observable<any>>) => {
  Object.entries(observables).forEach(([name, obs]) => obs.log(name))
}