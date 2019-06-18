# Peakon Code Challenge

Peakon code challenge. Implementing specified rest api as pr code challenge description.

The challenge was open ended in some regards, so the following choices has been made:

__Language__: The chosen language is [Clojure](https://www.clojure.com) with a [http-kit](https://www.http-kit.org) web server.

__Rest API__: The rest api url and parameter names are unspecified. They are as follows:
* __Service Port__: 8080 (configurable using HOST_PORT env var)
* __Service Path__: The rest-api is available under ```/v1/employee/```
* __Service Parameters__: The service support the following parameters:
  * ___page___ - requested page index
  * ___per_page___ - number of items to return per page

* __Service Values__ A successful service call will return the following response, where the field names should be self-explanatory (items field replaced with ... for brevity):
    ```json
    {
        "items": [...], 
        "items_total": 2,
        "page": 1,
        "prev_page": null,
        "next_page": null,
        "pages": 1,
        "per_page": 10,
        "search-term": "al"
    }
    ```

## Pre-Built Binary 

If Docker is installed, this implementation can be inspected with minimum hassle on a reviewer machine:

``` bash
$ sudo docker pull runebrinckmeyer/peakon-cc:1.0.0
$ sudo docker run --rm -p 8080:8080 runebrinckmeyer/peakon-cc:1.0.0
```

## Manual Building

Checkout code from this repository.

To run/build the code the following dependencies must be installed:

* Java (8+)
* Leiningen

This project is built using the build tool Leiningen which needs to be installed:
https://leiningen.org/

## Running Tests

Perform steps described under "Manual Building".

Tests are set up using the standard Clojure test framework. Eftest is added as a test-runner, while the standard test runner is available also.

Use either

```bash
$ lein eftest
```

or

``` bash
$ lein test
```

to run tests.

The tests are divided into:
* Verification of database integrity and query results 
* Verification of web-facing routes passing correct parameters
 
## Running

Start service using prebuilt or manual method. Eg either

```bash
$ sudo docker run --rm -p 8080:8080 runebrinckmeyer:peakon-cc:1.0.0
```

or 

```bash
$ lein run
```

Point your browser to ```http://localhost:8080``` to verify that it is running or
test the rest-api directly using eg. curl:

```bash
$ curl http://localhost:8080/v1/employee/donald
```


## Starting in REPL

In case an IDE REPL launcher is not available, a REPL can be started using

```bash
$ lein repl
```

server startup / shutdown is done via the pair of functions:

```clojure
(start-app)
(stop-app)
```

Running tests from with the REPL:

```clojure
rest-api.core=> (require '[eftest.runner :refer [find-tests run-tests]])
nil
rest-api.core=> (run-tests (find-tests "test"))
...
```

## Code Organization

The code pertaining the test is implemented 3 files:
* app.clj
* core.clj
* db.clj

core.clj is responsible for launching the application. 
app.clj sets up the routes and web server.
db.clj contains the loading and prepping of the actual data as well as the search function.

app.clj route /v1/employee/:search-string maps through to search-employee()


<!-- ## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version. -->
