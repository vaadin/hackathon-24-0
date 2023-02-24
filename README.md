# hackathon-24-0

## Tested personal add-ons with Vaadin 24

### Spinkit

Latest version works on Vaadin 24 without any change.
Upgraded demo project to Vaadin 24 and Jetty 11

https://vaadin.com/directory/component/spinkit-add-on


### Twitter widgets for Vaadin

Latest version works on Vaadin 24 without any change.
Upgraded demo projects to Vaadin 24, Jetty 11 and Quarkus 3.0.0.Alpha3
Used polymer-to-lit tool in the demo to convert a markdown helper and it worked fine.

https://vaadin.com/directory/component/twitter-widgets-for-vaadin


### Upgraded add-ons demo application to Vaadin 24

https://mbf-vaadin-addons.up.railway.app/


### Notes on migration

Add-ons are pretty simple and did not require changes.
For the demo projects some minor fixes like upgrading dependencies and replacing removed code.
I got some troubles in one project with the development bundle, but mainly because I did not
correctly handled assets in the custom theme.


## Documentation

Removed a section in MPR docs for Vaadin 8, with wrong contents,
possibly because of some mistake during conflicts resolution.
Thanks to @Ansku for the finding and reporting.
https://github.com/vaadin/docs/pull/2225
