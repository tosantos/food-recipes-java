# Food Recipes
This git repository showcases a simple food recipes application written using **Java 17**.

## Detail Overview
The app is built using Maven. 

It contains 4 modules:
* recipes-repo
* recipes-server
* recipes-cli
* recipes-client

#### recipes-repo
The repository that stores all recipe related information. 
This module depends on **h2** relational database system to support CRUD operations

#### recipes-server
The REST API server that exposes endpoints and performs CRUD operations on the repository
This module depends on **recipes-repo** and uses jersey to support REST API

#### recipes-cli
A simple CLI application that performs CRUD operations based on user input
This module depends on **recipes-repo** to interact with the repository

#### recipes-client
A simple REST API client that performs CRUD operations based on REST calls made via user input
This module depends on **recipes-server** to perform REST calls

## Running
I decided not to use a uber-jar approach for this project. 

If you want to run the different UI and REST server you'll need to run them manually or better yet use an IDE

## About
This repo is for my own educational purposes. Feel free to use if you find it useful.
