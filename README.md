# V24 hackathon / Olli

## Updating add-ons notes and findings

### Summary

Updated 5 Vaadin add-ons to Vaadin 24.0.0.beta4. Reported a handful of findings to the `#hackathon-24-0` channel.

Issues reported:
* https://github.com/vaadin/flow/issues/16036
* https://github.com/vaadin/addon-template/issues/279

Wrote an V14->V24 add-on upgrade guide: https://docs.google.com/document/d/1GpBIZZD65VMpXp2nwFHGG4AoYcu7Bo18WRPiZBdOfWA/edit#heading=h.cnrtccs4kxhg

## Add-on updates

### Preparatory steps:
* Made sure I have the relevant technologies (https://github.com/vaadin/platform/issues/3720), the most relevant being:
* Java 17
* Node 18
* (browsers should be automatically up to date and Java dependencies are handled in the projects)

### Browser Opener https://vaadin.com/directory/component/browser-opener/
Previous version 1.0.0: using V23.1.7 on Java 11
* Updated version number to 2.0.0
* Updated `maven.compiler.source` and `maven.compiler.target` properties to 17
* Updated Vaadin version number to 24.0.0.beta4
* As project was missing pluginRepositories for `vaadin-prereleases`, added
* Updated Java to 17 in IDE
  => Compilation fails because of PolymerTemplate
* Read https://vaadin.com/docs/next/upgrading
* Ran `mvn vaadin:convert-polymer`
  => Fails because `[ERROR] Failed to execute goal com.vaadin:vaadin-maven-plugin:24.0.0.beta4:convert-polymer (default-cli) on project browser-opener: Could not execute convert-polymer goal.: java.nio.file.NoSuchFileException: C:\dev\addons and components\browser-opener\node_modules\@vaadin\flow-frontend -> [Help 1]`
* Ran `mvn vaadin:clean-frontend` and `mvn vaadin:prepare-frontend` to regenerate the `/node_modules` directory
* Ran `mvn vaadin:convert-polymer`
  => Works now
* Ran `mvn jetty:run`
  => Fails `Caused by: java.lang.RuntimeException: Error scanning file .... Caused by: java.lang.IllegalArgumentException: Unsupported class file major version 61`
* Updated `jetty-maven-plugin` to version 11.0.13
* Removed `<scanIntervalSeconds>-1</scanIntervalSeconds>` and `<webAppConfig>` from the jetty plugin configuration because they are no longer valid
* Built and ran the project
  ==> Works
* Added `src/main/dev-bundle/` to `.gitignore` because this is an add-on
  ==> Created https://github.com/vaadin/addon-template/issues/279 as suggested by Leif
  ==> Artur created https://github.com/vaadin/flow/issues/16028 as a side-effect
* Ran `mvn install -Pdirectory` to create the Directory package
  => Fails because can't create a zip fale
* Updated `maven-assembly-plugin` to 3.4.2
* Ran `mvn install -Pdirectory` to create the Directory package again
  ==> Success
* Published to directory: https://vaadin.com/directory/component/browser-opener Version 2.0.0
* Sources: https://github.com/OlliTietavainenVaadin/browser-opener/tree/v24
* Apparently Jetty 11 uses Servlet 5 while the requirement is 6 so it may or may not work; Jetty 12 beta should be used instead once it's available

### ClipboardHelper https://vaadin.com/directory/component/clipboardhelper/
Previous version 1.2.0: Using V14.8.8 on Java 1.8
* Updated `maven.compiler.source` and `maven.compiler.target` properties to 17
* Updated Java to 17 in IDE
* Updated version number to 2.0.0
* Updated Vaadin version number to 24.0.0.beta4
* Updated `http` Maven repository links to `https`
* Build
  => Fails because of `@HtmlImport` and `PolymerTemplate` are not known
* Removed `@HtmlImport` and `.html` Polymer file from resources
* Ran `mvn vaadin:convert-polymer`
* Updated `jetty-maven-plugin` to version 11.0.13
* Removed `<scanIntervalSeconds>-1</scanIntervalSeconds>` and `<webAppConfig>` from the jetty plugin configuration because they are no longer valid
* Ran `mvn jetty:run` and tested the demo
* Updated `maven-assembly-plugin` to 3.4.2
* Ran `mvn install -Pdirectory` to create the Directory package
* Published to directory: https://vaadin.com/directory/component/clipboardhelper Version 2.0.0
* Sources: https://github.com/OlliTietavainenVaadin/clipboardhelper/tree/v24

### CssVariableSetter https://vaadin.com/directory/component/cssvariablesetter/
Previous version 1.0.0: using V10.0.9 on Java 1.8
* Updated `maven.compiler.source` and `maven.compiler.target` properties to 17
* Updated Java to 17 in IDE
* Updated version number to 2.0.0
* Updated Vaadin version number to 24.0.0.beta4
* Refactored removed `ui.getPage().executeJavaScript()` to `ui.getPage().executeJs()`
* Removed `@Theme(Lumo.class)` which doesn't work anymore
* Updated `jetty-maven-plugin` to version 11.0.13
* Removed `<scanIntervalSeconds>-1</scanIntervalSeconds>` and `<webAppConfig>` from the jetty plugin configuration because they are no longer valid
* Ran `mvn jetty:run` and tested the demo
* Updated `maven-assembly-plugin` to 3.4.2
* Ran `mvn install -Pdirectory` to create the Directory package
  => Fails because version mismatch in UI.class
* Updated `<pluginManagement>` section and added `vaadin-maven-plugin` configuration. See pom.xml diff in GitHub
* Ran `mvn install -Pdirectory` to create the Directory package again
* Published to Directory: https://vaadin.com/directory/component/cssvariablesetter Version 2.0.0
* Sources: https://github.com/OlliTietavainenVaadin/cssvariablesetter/tree/v24
* Added `/node_modules` and `frontend/generated/` to `.gitignore`

### File Download Wrapper https://vaadin.com/directory/component/file-download-wrapper/
Previous version 6.0.0: Using V23.3.6 on Java 11. Already updated to LitTemplate!
* Updated `maven.compiler.source` and `maven.compiler.target` properties to 17
* Updated Java to 17 in IDE
* Updated version number to 7.0.0
* Updated Vaadin version number to 24.0.0.beta4
* Updated `jetty-maven-plugin` to version 11.0.13
* Removed `<scanIntervalSeconds>-1</scanIntervalSeconds>` and `<webAppConfig>` from the jetty plugin configuration because they are no longer valid
* Updated `maven-assembly-plugin` to 3.4.2
* Removed `frontend-maven-plugin` from pom.xml
* Ran `mvn jetty:run` and tested the demo
* Ran `mvn install -Pdirectory` to create the Directory package
* Published to Directory: https://vaadin.com/directory/component/file-download-wrapper Version 7.0.0
* Added `/node_modules`, `src/main/dev-bundle/ and `frontend/generated/` to `.gitignore`
* Sources: https://github.com/OlliTietavainenVaadin/file-download-wrapper/tree/v24

### Fullscreen https://vaadin.com/directory/component/fullscreen/
Previous version 1.1.2: Using Vaadin 14.8.7 on Java 1.8

* Added `/node_modules`, `src/main/dev-bundle/ and `frontend/generated/` to `.gitignore`
* Updated `maven.compiler.source` and `maven.compiler.target` properties to 17
* Updated Java to 17 in IDE
* Updated version number to 7.0.0
* Updated Vaadin version number to 24.0.0.beta4
* Updated `jetty-maven-plugin` to version 11.0.13
* Removed `<scanIntervalSeconds>-1</scanIntervalSeconds>` and `<webAppConfig>` from the jetty plugin configuration because they are no longer valid
* Built the application
  => Fails because of HtmlImport and PolymerTemplate
* Removed HtmlImport from Java code and html source files
* As project was missing pluginRepositories for `vaadin-prereleases`, added
* Ran `mvn vaadin:clean-frontend` and `mvn vaadin:prepare-frontend`
* Ran `mvn vaadin:convert-polymer`
* Doesn't compile because a Model property getter for `boolean close` is generated as `isClose` instead of `getClose`. Manually renaming the method fixes it. The `isClose` is the standard naming, of course.
* Ran `mvn jetty:run`
  => Fails with `[vite]: Rollup failed to resolve import "@vaadin/vaadin-button/src/vaadin-button.js" from "C:/dev/addons and components/fullscreen/frontend/generated/jar-resources/fullscreen-button.js".`
* Updated the import manually
* Ran `mvn jetty:run` and tested the demo
* Ran `mvn install -Pdirectory` to create the Directory package
* Published to Directory: https://vaadin.com/directory/component/fullscreen Version 2.0.0
* Sources: https://github.com/OlliTietavainenVaadin/fullscreen/tree/v24
