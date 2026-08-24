import { build } from 'vite'
import vue from '@vitejs/plugin-vue'

await build({
  base: '/',
  plugins: [vue()],
  build: { outDir: 'dist', assetsDir: 'assets' }
})
console.log('BUILD_OK')
