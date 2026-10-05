/** @type {import('stylelint').Config} */
export default {
  plugins: ['stylelint-no-unsupported-browser-features'],
  reportDescriptionlessDisables: true,
  reportNeedlessDisables: true,
  rules: {
    'plugin/no-unsupported-browser-features': [
      true,
      { severity: 'error', ignorePartialSupport: true }
    ]
  }
}
