import { fixupConfigRules } from "@eslint/compat";
import typescriptEslint from "@typescript-eslint/eslint-plugin";
import prettier from "eslint-plugin-prettier";
import tsParser from "@typescript-eslint/parser";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";
import react from "eslint-plugin-react";
import jest from "eslint-plugin-jest";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});
export default [{
    ignores: ["**/node_modules", "**/build"],
}, ...fixupConfigRules(compat.extends("plugin:react-hooks/recommended", "prettier")), {
    plugins: {
        "@typescript-eslint": typescriptEslint,
        prettier,
        react,
        jest
    },

    languageOptions: {
        parser: tsParser,
    },

    rules: {
        "prettier/prettier": ["error"],
        eqeqeq: "warn",
    },

    files: ["**/*.ts", "**/*.tsx"]
}];
