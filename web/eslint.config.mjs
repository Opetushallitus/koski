// eslint.config.mjs
import { FlatCompat } from "@eslint/eslintrc"
import { fixupConfigRules } from "@eslint/compat"

import reactPlugin from "eslint-plugin-react"
import mochaPlugin from "eslint-plugin-mocha"
import tsParser from "@typescript-eslint/parser"
import js from "@eslint/js"
import tseslint from "typescript-eslint"
import globals from "globals"

import path from "node:path"
import { fileURLToPath } from "node:url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Bridge for old-style "extends" configs
const compat = new FlatCompat({ baseDirectory: __dirname })

export default [
  // Core ESLint recommended rules
  js.configs.recommended,

  // TypeScript ESLint recommended rules
  ...tseslint.configs.recommended,

  // React + React Hooks via compat (no flat exports yet)
  ...fixupConfigRules(compat.extends("plugin:react/recommended")),
  ...fixupConfigRules(compat.extends("plugin:react-hooks/recommended")),

  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.mocha,
        __webpack_nonce__: true,
      },
      parser: tsParser,
      ecmaVersion: 2022,
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
          experimentalObjectRestSpread: true,
        },
      },
    },

    settings: {
      react: {
        version: "detect",
      },
    },

    rules: {
      // General
      "no-undef": "warn",
      "no-var": "off",
      "no-unreachable": "error",
      "no-console": "off",
      "no-warning-comments": "off",
      "no-unused-vars": "off",
      "array-callback-return": "off",
      eqeqeq: "warn",
      "no-shadow": "off",
      "prefer-spread": "warn",
      "prefer-regex-literals": "off",

      // React
      "react/jsx-no-undef": "warn",
      "react/jsx-uses-react": "warn",
      "react/jsx-uses-vars": "warn",
      "react/jsx-no-literals": "warn",
      "react/no-unknown-property": "warn",
      "react/react-in-jsx-scope": "warn",
      "react/self-closing-comp": ["warn", { component: true, html: false }],
      "react/jsx-wrap-multilines": "warn",
      "react/prop-types": "off",
      "react/display-name": "off",
      "react/jsx-key": "off",
      "react/no-string-refs": "off",
      "react/no-render-return-value": "off",

      // TypeScript
      "@typescript-eslint/no-shadow": "warn",
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-unused-vars": "off",
      "@typescript-eslint/ban-ts-comment": "off",
      "@typescript-eslint/no-empty-function": "off",
      "@typescript-eslint/no-unnecessary-type-constraint": "warn",
      "@typescript-eslint/ban-types": "off",
      "@typescript-eslint/no-unused-expressions": "off",
    },
  },

  // TypeScript-specific overrides
  {
    files: ["**/*.ts", "**/*.tsx"],
    rules: {
      "no-undef": "off",
    },
  },

  // Test overrides (Mocha)
  {
    files: ["test/**/*"],
    rules: {
      "no-unused-vars": "off",
      "no-undef": "off",
      "no-var": "off",
      camelcase: "off",
      "no-shadow": "warn",
      "no-extend-native": "off",

      // Mocha-specific
      "mocha/no-mocha-arrows": "off",
      "mocha/max-top-level-suites": "off",
      "mocha/consistent-spacing-between-blocks": "off",
      "mocha/no-setup-in-describe": "off",
      "mocha/no-sibling-hooks": "off",
      "mocha/no-identical-title": "off",
    },
  },
]
