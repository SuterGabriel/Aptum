# Das Frontend: Angular bauen, mit nginx ausliefern.
#
# nginx tut hier zwei Dinge: Es liefert die gebaute Anwendung aus, und es
# ist der Reverse Proxy, der /api an die beiden Dienste weiterreicht - im
# Betrieb dieselbe Aufgabe, die in der Entwicklung proxy.conf.json hat.

# ---- Stufe 1: bauen ------------------------------------------------------
FROM node:24-alpine AS bau
WORKDIR /bau
COPY package.json package-lock.json ./
# --ignore-scripts wie in der CI: Kein Paket führt beim Installieren Code aus.
RUN npm ci --no-audit --no-fund --ignore-scripts
COPY . .
RUN npm run build

# ---- Stufe 2: ausliefern -------------------------------------------------
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=bau /bau/dist/aptum/browser /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -q -O /dev/null http://localhost/ || exit 1
