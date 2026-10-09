import eslintReact from '@eslint-react/eslint-plugin'
import compatPlugin from 'eslint-plugin-compat'
import importXPlugin from 'eslint-plugin-import-x'
import eslintConfigPrettier from 'eslint-config-prettier'
import mochaPlugin from 'eslint-plugin-mocha'
import reactHooksPlugin from 'eslint-plugin-react-hooks'
import globals from 'globals'
import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'
import { includeIgnoreFile } from 'eslint/config'
import path from 'node:path'

export default [
  includeIgnoreFile(path.resolve(import.meta.dirname, '.gitignore')),
  {
    plugins: { 'react-hooks': reactHooksPlugin },
    rules: {
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'error'
    }
  },
  {
    plugins: { '@eslint-react': eslintReact },
    rules: {
      '@eslint-react/jsx-no-comment-textnodes': 'error',
      '@eslint-react/jsx-no-children-prop': 'error',
      '@eslint-react/no-direct-mutation-state': 'error',
      '@eslint-react/no-component-will-mount': 'error',
      '@eslint-react/no-component-will-receive-props': 'error',
      '@eslint-react/no-component-will-update': 'error',
      '@eslint-react/dom-no-dangerously-set-innerhtml-with-children': 'error',
      '@eslint-react/dom-no-find-dom-node': 'error',
      '@eslint-react/dom-no-unsafe-target-blank': 'error',
      '@eslint-react/dom-no-unknown-property': 'error'
    }
  },
  eslint.configs.recommended,
  { ...mochaPlugin.configs.recommended, files: ['test/**/*'] },
  { ...compatPlugin.configs['flat/recommended'], files: ['app/**/*'] },
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
        __webpack_nonce__: true
      },

      parser: tseslint.parser,
      sourceType: 'module',

      parserOptions: {
        ecmaFeatures: {
          jsx: true
        }
      }
    },

    rules: {
      'no-undef': 'error',
      'no-unreachable': 'error',
      'no-unused-vars': 'off',
      'no-restricted-syntax': [
        'error',
        {
          selector: 'JSXText[value=/[\\p{L}\\p{N}]/u]',
          message:
            'Käytä lokalisoitua tekstiä (t, Trans) JSX-literaalin sijaan.'
        }
      ],
      eqeqeq: 'error',
      'no-shadow': 'off',
      'prefer-spread': 'error',
      '@typescript-eslint/no-shadow': 'error',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/ban-ts-comment': [
        'error',
        { 'ts-expect-error': false }
      ],
      '@typescript-eslint/no-empty-function': 'off',
      '@typescript-eslint/no-unnecessary-type-constraint': 'error',
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
      'no-undef': 'off', // Mochan takia
      'no-var': 'off', // Mochan takia
      'no-shadow': 'error', // Mochan takia
      'mocha/no-exclusive-tests': 'error',
      'mocha/no-pending-tests': 'error',
      'mocha/no-mocha-arrows': 'off',
      'mocha/max-top-level-suites': 'off',
      'mocha/consistent-spacing-between-blocks': 'off',
      'mocha/consistent-structure': 'off',
      'mocha/no-identical-title': 'off'
    }
  },
  {
    plugins: { 'import-x': importXPlugin },
    rules: { 'import-x/no-extraneous-dependencies': 'error' }
  },
  {
    files: ['app/**/*'],
    rules: {
      'import-x/no-extraneous-dependencies': [
        'error',
        { devDependencies: false }
      ]
    }
  },
  eslintConfigPrettier,
  { rules: { 'no-unexpected-multiline': 'error' } }
]
