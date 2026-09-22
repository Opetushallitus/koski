export const hasFeatureFlag = (flag: string): boolean =>
  localStorage.getItem(flag) !== null ||
  new URLSearchParams(window.location.search).has(flag)
