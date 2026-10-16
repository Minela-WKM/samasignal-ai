## 2026-10-16 - Add security headers to Nginx config
**Vulnerability:** The web frontend (served via Nginx) was missing foundational security headers (X-Frame-Options, X-Content-Type-Options, Referrer-Policy, and Content-Security-Policy), leaving it susceptible to clickjacking and cross-site injection attacks.
**Learning:** The frontend is served using a barebones Nginx configuration in the Docker container (`apps/web/nginx.conf`). It acts as a reverse proxy for the API, so security headers need to be added directly to this entry point.
**Prevention:** Include standard security headers in the base Nginx configuration template for any new web application deployment to ensure defense-in-depth from the start.
