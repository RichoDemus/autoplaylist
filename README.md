#Richo Reader

## Instructions

### Check for outdated dependencies
    ./gradlew dependencyUpdates

## reader-test on raspberry:
* Add -H tcp://127.0.0.1:2375 to the docker process (like DOCKER_OPTS or systemd)
* Add the env variable DOCKER_HOST=tcp://127.0.0.1:2375

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

## Design
### Services
* UserService, login: username, password -> session(userId)
* LabelService, getLabels: userId -> labels
* FeedSerice, getFeed: feedId -> items
* SubscribedFeedsService, getFeeds: userId -> List<Pair<feedId, #unreadItems>>
* ReadItemsService, getReadItems: userId, feedId -> readItems
### Explanation
When an item is marked as read in ReadItemsService, a message will also go to SubscribedFeedsService which will decrement its counter for that feed
