const path = require('path');

module.exports = {
  entry: './src/index.ts',
  mode: 'production',
  module: {
    rules: [
      {
        use: 'ts-loader',
        exclude: /node_modules/,
      },
    ],
  },
  target: 'node',
  externals: {  
    'sharp': 'commonjs sharp',
    bufferutil: "bufferutil",
    "utf-8-validate": "utf-8-validate",
    'pino-pretty': 'pino-pretty',
    "link-preview-js": "link-preview-js",
    'qrcode-terminal': 'qrcode-terminal',
    "jimp": "jimp",
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: 'index.js',
    path: path.resolve(__dirname, 'build'),
  },
};