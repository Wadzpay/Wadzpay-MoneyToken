module.exports = function (api) {
  api.cache(true)
  return {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    [
      "babel-plugin-root-import",
      {
        paths: [
          {
            rootPathPrefix: "~images",
            rootPathSuffix: "assets/images"
          },
          {
            rootPathPrefix: "~",
            rootPathSuffix: "src"
          }
        ]
      }
    ]
  ]
}
};
