FROM node:10.6.0-alpine

RUN apk --no-cache add git

RUN mkdir /app
WORKDIR /app
COPY package.json /app/
COPY package-lock.json /app/
RUN npm ci

COPY public /app/public
COPY src /app/src
COPY tslint.json /app/
RUN CI=true npm test && npm run build && npm install -g serve

ENV NODE_ENV=production
CMD serve -s build

EXPOSE 5000
