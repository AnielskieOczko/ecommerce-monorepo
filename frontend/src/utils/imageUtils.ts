export const getImageUrl = (imagePath: string): string => {
  if (imagePath.startsWith('http')) {
    return imagePath;
  }
  // Extract just the filename from the path
  const filename = imagePath.split('/').pop();
  return `http://localhost:8080/api/v1/public/products/images/${filename}`;
};
