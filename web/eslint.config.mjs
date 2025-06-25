import { fixupConfigRules, fixupPluginRules } from '@eslint/compat'
import reactPlugin from 'eslint-plugin-react'
import mochaPlugin from 'eslint-plugin-mocha'
import typescriptEslint from '@typescript-eslint/eslint-plugin'
import globals from 'globals'
import tsParser from '@typescript-eslint/parser'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import js from '@eslint/js'
import { FlatCompat } from '@eslint/eslintrc'
import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all
})

export default [
  ...fixupConfigRules(compat.extends('plugin:react-hooks/recommended')),
  eslint.configs.recommended,
  reactPlugin.configs.flat.recommended,
  mochaPlugin.configs.flat.recommended,
  ...tseslint.configs.recommended,
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.mocha,
        __webpack_nonce__: true
      },

      parser: tsParser,
      ecmaVersion: 6,
      sourceType: 'module',

      parserOptions: {
        ecmaFeatures: {
          jsx: true,
          experimentalObjectRestSpread: true
        }
      }
    },

    settings: {
      react: {
        version: 'detect'
      }
    },

    rules: {
      'no-undef': 'warn',
      'no-var': 'off',
      'no-unreachable': 'error',
      'no-console': 'off',
      'no-warning-comments': 'off',
      'no-unused-vars': 'off',
      'react/jsx-no-undef': 'warn',
      'react/jsx-uses-react': 'warn',
      'react/jsx-uses-vars': 'warn',
      'react/jsx-no-literals': 'warn',
      'react/no-unknown-property': 'warn',
      'react/react-in-jsx-scope': 'warn',

      'react/self-closing-comp': [
        'warn',
        {
          component: true,
          html: false
        }
      ],

      'react/jsx-wrap-multilines': 'warn',
      'react/prop-types': 'off',
      'react/display-name': 'off',
      'react/jsx-key': 'off',
      'react/no-string-refs': 'off',
      'array-callback-return': 'off',
      'react/no-render-return-value': 'off',
      'prefer-regex-literals': 'off',
      eqeqeq: 'warn',
      'no-shadow': 'off',
      'prefer-spread': 'warn',
      '@typescript-eslint/no-shadow': 'warn',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/ban-ts-comment': 'off',
      '@typescript-eslint/no-empty-function': 'off',
      '@typescript-eslint/no-unnecessary-type-constraint': 'warn',
      '@typescript-eslint/ban-types': 'off',
      '@typescript-eslint/no-unused-expressions': 'off'
    }
  },
  {
    files: ['**/*.ts', '**/*.tsx'],

    rules: {
      'no-undef': 'off'
    }
  },
  {
    files: ['test/**/*'],
    rules: {
      'no-unused-vars': 'off', // Mochan takia
      'no-undef': 'off', // Mochan takia
      'no-var': 'off', // Mochan takia
      camelcase: 'off',
      'no-shadow': 'warn', // Mochan takia
      'no-extend-native': 'off', // Mochan takia
      'mocha/no-mocha-arrows': 'off',
      'mocha/max-top-level-suites': 'off',
      'mocha/consistent-spacing-between-blocks': 'off',
      'mocha/no-setup-in-describe': 'off',
      'mocha/no-sibling-hooks': 'off',
      'mocha/no-identical-title': 'off'
    }
  }
]
