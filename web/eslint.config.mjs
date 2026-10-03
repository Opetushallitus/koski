import eslintReact from '@eslint-react/eslint-plugin'
import compatPlugin from 'eslint-plugin-compat'
import eslintConfigPrettier from 'eslint-config-prettier'
import mochaPlugin from 'eslint-plugin-mocha'
import reactHooksPlugin from 'eslint-plugin-react-hooks'
import globals from 'globals'
import eslint from '@eslint/js'
import tseslint from 'typescript-eslint'

export default [
  {
    plugins: { 'react-hooks': reactHooksPlugin },
    rules: {
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn'
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
      '@eslint-react/dom-no-unknown-property': 'warn'
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
      ecmaVersion: 6,
      sourceType: 'module',

      parserOptions: {
        ecmaFeatures: {
          jsx: true,
          experimentalObjectRestSpread: true
        }
      }
    },

    rules: {
      'no-undef': 'warn',
      'no-var': 'off',
      'no-unreachable': 'error',
      'no-console': 'off',
      'no-warning-comments': 'off',
      'no-unused-vars': 'off',
      'no-restricted-syntax': [
        'warn',
        {
          selector: 'JSXText[value=/[\\p{L}\\p{N}]/u]',
          message:
            'Käytä lokalisoitua tekstiä (t, Trans) JSX-literaalin sijaan.'
        }
      ],
      'array-callback-return': 'off',
      'prefer-regex-literals': 'off',
      eqeqeq: 'error',
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
      'mocha/no-mocha-arrows': 'off',
      'mocha/max-top-level-suites': 'off',
      'mocha/consistent-spacing-between-blocks': 'off',
      'mocha/consistent-structure': 'off',
      'mocha/no-identical-title': 'off'
    }
  },
  eslintConfigPrettier,
  { rules: { 'no-unexpected-multiline': 'error' } }
]
