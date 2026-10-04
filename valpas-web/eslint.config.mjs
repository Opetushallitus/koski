import tseslint from "typescript-eslint"
import eslintConfigPrettier from "eslint-config-prettier"
import compat from "eslint-plugin-compat"
import reactHooks from "eslint-plugin-react-hooks"

export default [
  {
    ignores: ["**/node_modules", "**/dist", "**/.cache"],
  },
  {
    plugins: { "react-hooks": reactHooks },
    rules: {
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
    },
  },
  { ...compat.configs["flat/recommended"], files: ["src/**/*"] },
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
