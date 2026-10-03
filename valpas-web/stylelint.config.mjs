export default {
  customSyntax: "postcss-less",
  plugins: ["stylelint-no-unsupported-browser-features"],
  reportDescriptionlessDisables: true,
  reportNeedlessDisables: true,
  rules: {
    "plugin/no-unsupported-browser-features": [
      true,
      {
        severity: "error",
        ignorePartialSupport: true,
        // LESS purkaa sisäkkäiset säännöt.
        ignore: ["css-nesting"],
      },
    ],
  },
}
