# Swagger Zuul Filter

this is a very simple application exposing a Zuul Filter.
the idea is to use Zuul as a reverse proxy to serve an API.
When the Zuul is hit with an API request, the filter will check toward the underlying API
if the requested endpoint is effectively exposed. To do so, it'll check the Route and the httpMethod called 
against the underlying API exposed swagger json document.