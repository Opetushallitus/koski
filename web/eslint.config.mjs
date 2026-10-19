import eslintReact from '@eslint-react/eslint-plugin'
import compatPlugin from 'eslint-plugin-compat'
import importXPlugin from 'eslint-plugin-import-x'
import eslintConfigPrettier from 'eslint-config-prettier'
import mochaPlugin from 'eslint-plugin-mocha'
import reactHooksPlugin from 'eslint-plugin-react-hooks'
import globals from 'globals'
import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores, includeIgnoreFile } from 'eslint/config'
import path from 'node:path'
import confusingBrowserGlobals from './eslint/confusingBrowserGlobals.mjs'

export default defineConfig([
  includeIgnoreFile(path.resolve(import.meta.dirname, '.gitignore')),
  globalIgnores(['app/util/bacon.atom.js']),
  {
    plugins: {
      'react-hooks': /** @type {import('eslint').ESLint.Plugin} */ (
        reactHooksPlugin
      )
    },
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
    files: ['**/*.{js,jsx,ts,tsx}'],
    ignores: ['app/**/*.{js,jsx}'],
    languageOptions: { globals: globals.node }
  },
  {
    files: ['app/**/*.{js,jsx}'],
    extends: [
      eslintReact.configs.recommended,
      reactHooksPlugin.configs.flat.recommended,
      eslintReact.configs['disable-conflict-eslint-plugin-react-hooks'],
      importXPlugin.configs['flat/recommended']
    ],
    settings: {
      'import-x/resolver': {
        node: { extensions: ['.js', '.jsx', '.ts', '.tsx', '.json'] }
      },
      'import-x/extensions': ['.js', '.jsx', '.ts', '.tsx'],
      'import-x/parsers': {
        '@typescript-eslint/parser': ['.ts', '.tsx']
      }
    },
    rules: {
      '@eslint-react/no-unused-class-component-members': 'error',
      '@eslint-react/jsx-no-children-prop': 'error',
      '@eslint-react/jsx-no-comment-textnodes': 'error',
      '@eslint-react/jsx-no-leaked-dollar': 'error',
      '@eslint-react/jsx-no-leaked-semicolon': 'error',
      '@eslint-react/dom-no-script-url': 'error',
      '@eslint-react/dom-no-string-style-prop': 'error',
      '@eslint-react/no-unused-state': 'error',
      '@eslint-react/no-unstable-context-value': 'error',
      '@eslint-react/dom-no-missing-iframe-sandbox': 'error',
      '@eslint-react/exhaustive-deps': 'error',
      'import-x/no-self-import': 'error',
      'import-x/no-useless-path-segments': 'error',
      'import-x/no-mutable-exports': 'error',
      'import-x/first': 'error',
      'import-x/no-commonjs': 'error',
      'import-x/no-absolute-path': 'error',
      'accessor-pairs': 'error',
      'array-callback-return': 'error',
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'no-constructor-return': 'error',
      'no-eval': 'error',
      'no-extend-native': 'error',
      'no-implied-eval': 'error',
      'no-invalid-this': 'error',
      'no-loop-func': 'error',
      'no-new-func': 'error',
      'no-new-wrappers': 'error',
      'no-promise-executor-return': 'error',
      'no-return-assign': ['error', 'except-parens'],
      'no-self-compare': 'error',
      'no-sequences': ['error', { allowInParentheses: false }],
      'no-throw-literal': 'error',
      'no-unmodified-loop-condition': 'error',
      'no-unreachable-loop': 'error',
      'no-unsafe-negation': ['error', { enforceForOrderingRelations: true }],
      'no-unsafe-optional-chaining': [
        'error',
        { disallowArithmeticOperators: true }
      ],
      'no-unused-vars': ['error', { ignoreRestSiblings: true }],
      // Salli myöhemmin määritellyt apurit sisäkkäisissä funktioissa.
      'no-use-before-define': [
        'error',
        {
          functions: false,
          classes: false,
          variables: false,
          allowNamedExports: true
        }
      ],
      'no-unused-expressions': [
        'error',
        {
          allowShortCircuit: true,
          allowTernary: true,
          enforceForJSX: true
        }
      ],
      'no-template-curly-in-string': 'error',
      'no-var': 'error',
      'prefer-const': ['error', { destructuring: 'all' }],
      'no-restricted-globals': ['error', ...confusingBrowserGlobals],
      'require-atomic-updates': 'error',
      // Vääriä osumia Bacon/baret-koodissa tai liikaa korjattavaa vanhassa koodissa
      '@eslint-react/no-array-index-key': 'off',
      '@eslint-react/no-missing-key': 'off'
    }
  },
  {
    files: ['app/dokumentaatio/**'],
    rules: { '@eslint-react/dom-no-dangerously-set-innerhtml': 'off' }
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
])
