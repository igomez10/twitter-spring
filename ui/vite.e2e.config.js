import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig({
    plugins: [react()],
    server: {
        host: "127.0.0.1",
        port: 14173,
        proxy: {
            "/api": {
                target: "http://127.0.0.1:18080",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, "")
            }
        }
    }
});
