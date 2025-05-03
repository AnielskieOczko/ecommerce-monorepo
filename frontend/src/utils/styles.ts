/**
 * Merges class names and filters out falsy values
 */
export const cn = (...classes: (string | undefined | null | false)[]) => {
  return classes.filter(Boolean).join(' ');
};

/**
 * Creates a variant class mapping with base classes
 */
export const createVariants = <T extends Record<string, string>>(
  base: string,
  variants: T
): Record<keyof T, string> => {
  const result = {} as Record<keyof T, string>;
  for (const [key, value] of Object.entries(variants)) {
    result[key as keyof T] = `${base} ${value}`.trim();
  }
  return result;
};
