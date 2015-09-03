#Richo Reader

## Features TODO
* Add JWT Authentication
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

## Bugs
* Currently downloads all items in a feed if it's outdated, could perhaps only download new items
