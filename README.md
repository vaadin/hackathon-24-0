# Hackathon 24.0.0

This is an hybrid project (with views in Flow and Hilla) generated in star.vaadin.com for vaadin 23.3.6 and hilla 1.3.6


### Achievements

1. Application Migrated from 23.3 to 24.0 by following upgrade guide, without any issue
2. Make master-detail java view responsive. It tries to fix [issue](https://github.com/vaadin/start/issues/856). Here the screenshots:
<img width="300" src="./images/original.png">
<img width="300" src="./images/responsive.png">

3. Add MPR dependencies, my idea was put v24-flow-spreadsheet and v8-addon-spreadsheet side by side in order to compare visually and performance of both implementations.

Trying it, I found two issues:
 - that they use different version of the POI library, making impossible to run both in the same app
 - legacy license checker fails because some issue in vaadin license server, I reported it.

4. Then I decided replace v24-flow-spreadsheet in the view by v8 one, I succeeded, but styles are broken although all dom elements, hence, it needs more work, probably css compilation does not find spreadsheet stuff. Found this issue:
 - `VaadinWebSecurity` stop serving `/framework` requests, fixed in code, but probably it needs futher investigation and reporting




