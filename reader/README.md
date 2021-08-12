#Richo Reader

## Instructions

### Run
```
docker run yadda yadda -e GCS_PROJECT=X -e GCS_BUCKET=Y`
```

### Compile and run all test
```
.\mvnw verify
```

### Check for outdated dependencies
```
.\mvnw versions:display-dependency-updates
```

### Deploy
```
docker build -t richodemus/reader .
docker push
docker-compose pull #on server
docker rm -f reader
docker-compose up -d
```

## Features TODO
* Use my classpath scanner
* add a captcha for signups
* make storing username and password/token a part of Api.js
* Use SSE to push data to clients
* Rewrite api to send less data at a time, like only feeds first and then items when expanded
    * maybe actually use Falcor?
* Add the ability to import feeds
    * Google reader
    * feedly
    * The old reader
* ability to put feeds in different categories
* option/checkbox to hide empty feeds
* mechanism to detect dead feeds
* Add 2-factor authentication
* Add feed name to item list
* Split channels into subchannels using keywords/regex
* Some way to bulk-store ranges of read items, like "this item and all older are read"
* look into https://github.com/palantir/dropwizard-version-info for inspiration for version info

## Design
### Services
* UserService, login: username, password -> session(userId)
* LabelService, getLabels: userId -> labels
* FeedSerice, getFeed: feedId -> items
* SubscribedFeedsService, getFeeds: userId -> List<Pair<feedId, #unreadItems>>
* ReadItemsService, getReadItems: userId, feedId -> readItems
### Explanation
When an item is marked as read in ReadItemsService, a message will also go to SubscribedFeedsService which will decrement its counter for that feed

## Scripts
Create user:
```curl -X POST --header "Content-Type: application/json" --header "Accept: application/json" -d '{"username":"RichoDemus","password":"funnay","inviteCode":"iwouldlikeaninvitepleaseletmesignuptotestthis"}' http://localhost:8080/api/users```
