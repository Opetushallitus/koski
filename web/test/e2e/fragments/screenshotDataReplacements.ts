import { Page } from '@playwright/test'

export type ScreenshotTextReplacementRule = {
  selector: string
  replacement: string
  pattern?: string
}

const replacementRules: ScreenshotTextReplacementRule[] = [
  {
    selector:
      '[data-testid$=".opiskeluoikeus.oid"], .opiskeluoikeus .id .value, .opiskeluoikeus-button .id .value',
    pattern: '1\\.2\\.246\\.562\\.15\\.\\d{11}',
    replacement: '1.2.246.562.15.00000000001'
  }
]

declare global {
  interface Window {
    __koskiScreenshotData?: Array<{ node: Text; originalValue: string }>
  }
}

export type ScreenshotDataReplacement = {
  restore: () => Promise<void>
}

export const replaceScreenshotData = async (
  page: Page
): Promise<ScreenshotDataReplacement> => {
  await page.evaluate((rules: ScreenshotTextReplacementRule[]) => {
    const findTextNodes = (element: Element): Text[] => {
      const walker = document.createTreeWalker(element, NodeFilter.SHOW_TEXT)
      const nodes: Text[] = []
      let node = walker.nextNode()

      while (node !== null) {
        nodes.push(node as Text)
        node = walker.nextNode()
      }

      return nodes
    }

    const preparedRules = rules.map((rule) => {
      const elements = Array.from(document.querySelectorAll(rule.selector))
      const selectedTextNodes = new Set<Text>()

      elements.flatMap(findTextNodes).forEach((node) => {
        selectedTextNodes.add(node)
      })

      return {
        ...rule,
        pattern:
          rule.pattern === undefined
            ? undefined
            : new RegExp(rule.pattern, 'g'),
        textNodes: selectedTextNodes
      }
    })

    const originalValues = new Map<Text, string>()

    for (const rule of preparedRules) {
      for (const node of rule.textNodes) {
        const originalValue = node.nodeValue ?? ''
        const replacedValue =
          rule.pattern === undefined
            ? originalValue.trim().length === 0
              ? originalValue
              : rule.replacement
            : originalValue.replace(rule.pattern, rule.replacement)

        if (replacedValue !== originalValue) {
          if (!originalValues.has(node)) {
            originalValues.set(node, originalValue)
          }
          node.nodeValue = replacedValue
        }
      }
    }

    window.__koskiScreenshotData = Array.from(
      originalValues,
      ([node, originalValue]) => ({ node, originalValue })
    )
  }, replacementRules)

  return {
    restore: async () => {
      await page.evaluate(() => {
        for (const { node, originalValue } of window.__koskiScreenshotData ??
          []) {
          node.nodeValue = originalValue
        }
        delete window.__koskiScreenshotData
      })
    }
  }
}
