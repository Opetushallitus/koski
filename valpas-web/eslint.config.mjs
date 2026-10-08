import js from "@eslint/js"
import tseslint from "typescript-eslint"
import eslintConfigPrettier from "eslint-config-prettier"
import compat from "eslint-plugin-compat"
import importX from "eslint-plugin-import-x"
import reactHooks from "eslint-plugin-react-hooks"
import globals from "globals"

export default [
  {
    ignores: ["**/node_modules", "**/dist", "**/.cache"],
  },
  js.configs.recommended,
  {
    files: ["**/*.js"],
    languageOptions: { sourceType: "commonjs", globals: globals.node },
  },
  { files: ["**/*.mjs"], languageOptions: { globals: globals.node } },
  { files: ["test/**/*.js"], languageOptions: { globals: globals.jest } },
  {
    plugins: { "react-hooks": reactHooks },
    rules: {
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
    },
  },
  { ...compat.configs["flat/recommended"], files: ["src/**/*"] },
  {
    plugins: { "import-x": importX },
    rules: { "import-x/no-extraneous-dependencies": "error" },
  },
  {
    files: ["src/**/*"],
    ignores: ["src/**/*.test.*", "src/utils/tests.ts"],
    rules: {
      "import-x/no-extraneous-dependencies": [
        "error",
        { devDependencies: false },
      ],
    },
  },
  eslintConfigPrettier,
  { rules: { "no-unexpected-multiline": "error" } },
  ...tseslint.configs.recommendedTypeChecked.map((config) => ({
    ...config,
    files: ["**/*.ts", "**/*.tsx"],
  })),
  {
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },

    rules: {
      eqeqeq: "error",
      "@typescript-eslint/ban-ts-comment": "error",
      "@typescript-eslint/no-duplicate-type-constituents": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
          destructuredArrayIgnorePattern: "^_",
          ignoreRestSiblings: true,
        },
      ],
      "@typescript-eslint/no-floating-promises": [
        "error",
        { ignoreVoid: false },
      ],
      "@typescript-eslint/no-misused-promises": [
        "error",
        { checksVoidReturn: { attributes: false } },
      ],
    },

    files: ["**/*.ts", "**/*.tsx"],
  },
]
