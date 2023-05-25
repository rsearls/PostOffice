# postoffice
This project is an exploration of strategies for running existing
webservices tests in the cloud (running wildfly).  The existing tests
use Junit4 and Arquillian.  The objective is to make minimal changes
to the existed test framework and identify what code changes are required.

Various cloud components (docker images, pods) may be protoTyped and evaluated.
Various cloud test frameworks may be protoTyped and evaluated as well.

Modules
- postoffice-core: app that uploads WAR files.
- mock-arquillian: arquillian components used in the tests that whose functionality is overridden and thus ignored.
- research: small projects building docker images with different plugins.
- proto-test: a single test using mock-arquillian and running a testcase against a wildfly based docker image.


