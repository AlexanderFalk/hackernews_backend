# Technical Handover Document

by ***Alexander, Danny, Kim***

## System overview/general introduction

For system scope, specification, introduction, implementation and much more, see our [Requirement Analysis Document](https://github.com/KIMB0/LSD_frontend/blob/master/Documents/Requirements%20Analysis%20Document.md).

## System architecture

Right now we (only) have two separate components running. A front-end (made in the Angular framework) and a back-end (made in REST).

* The **Front-end** runs on a remote droplet server, located [here](http://165.227.162.254/dist/news).
* The **Back-end** runs on another remote server, located [here](**insert-link-here**):

## Credentials

The base systems runs on projects that doesn't require credentials.
For server operations/administration, passwords are, of course, needed. For security reasons, we can't post that information here. Contact the authors of this document directly for access, or submit an issue [at the remote GitHub repository](https://github.com/AlexanderFalk/hackernews_backend/issues) with the label "server password request".

## Dataflow

As previously listed, the system consist of two components - the front-end and the back-end.

1. The back-end REST API provides Restful services that can be accessed at different URIs. The back-end also contains a [MongoDB](https://www.mongodb.com/what-is-mongodb) database that holds the different states of the users and items that are posted, put or deleted from the system.

2. The front-end user interface, assimilating the original HackerNews, will call the back-end REST API at the appropriate URIs that match the end-users request. For example, if the user submits a new item, the front-end will call a POST-action on the REST API at ***hackernews/item/*** .

3. The back-end REST API will then save the new item details in the database, if the information applied is valid.

4. The front-end can now do post a GET-request for all items or for the specific created item and show it to the user.

## Bug reports/issues

Found a bug? Please create an issue [at the remote GitHub repository](https://github.com/AlexanderFalk/hackernews_backend/issues) with the "bug" label, and supply as many details regarding the bug as possible, including:

* What caused the bug?
* Stacktrace and title of a possibly exception.
* Screenshot of the crash/error/error message or the like.
* Other possible helpful details.

## Restarting components

The servers hosting the components reside here (refer to the [Credentials](#credentials) section to get login details):

**Front-end**: is hosted by DigitalOcean, and resides [here](https://cloud.digitalocean.com/droplets/63474978/).

**Back-end**: is hosted by Hetzner, and resides [here](https://www.hetzner.com/virtual-server/cx40).

Restart each component from here. Alternatively, you can contact the authors of this document *or* create an issue on the [official repository](https://github.com/AlexanderFalk/hackernews_backend/issues) with the label "restart request" along with a description of why the restart is wanted.

## Accessing inner state information

To gain in-depth information/access of databases, log files etc., contact the authors of this document directly, or leave an issue at the [official repository](https://github.com/AlexanderFalk/hackernews_backend/issues) with the label "information wanted".