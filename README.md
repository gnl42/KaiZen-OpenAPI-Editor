# SwagEdit - Eclipse Editor for Swagger–OpenAPI

SwagEdit is an Eclipse Editor for the [Swagger](http://swagger.io) API Description Language, now known as the [OpenAPI Specification](http://openapis.org).

SwagEdit was developed for [RepreZen API Studio](http://reprezen.com/swagger-tools), a comprehensive solution for API modeling, documentation, visualization, testing and code generation, built on Eclipse. Swagger–OpenAPI.

We welcome your suggestions and contributions!

## Watch a short video about SwagEdit
[![Editing Swagger-OpenAPI in RepreZen API Studio](http://img.youtube.com/vi/KX_tHp_KQkE/0.jpg)](https://www.youtube.com/watch?v=KX_tHp_KQkE)

## Install SwagEdit
### Use a published update site 
You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site. Use "http://products.modelsolv.com/swagedit/kepler/" as URL. 

## User Documentation
Read more about SwagEdit features on http://www.reprezen.com/swagger-tools

## Development

Development of SwagEdit should be done with Eclipse. This project uses Maven/Tycho so you should have the Eclipse maven plugin `m2e` 
already installed.

To start developing SwagEdit, clone the repository with the following command:

```
git clone git@github.com:ModelSolv/SwagEdit.git
``` 

Open Eclipse and select `File > Import... > Maven > Existing Maven Project` and select the folder SwagEdit.
This will put the project SwagEdit into your current workspace. 

### Build a local update site from sources

From inside your SwagEdit folder, run the following command:

```
mvn clean verify
```

This command will build the project and generate an update site under the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository`.

You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site.
Click on `Local...` and select the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository` and then click `Ok`.

You can now select and install SwagEdit from the Eclipse update manager.

## Troubleshooting
### Autoformat (Ctrl+Shift+F) removes YAML comments 
This is a [known issue](https://github.com/oyse/yedit/issues/15) of YEdit, the YAML Editor on which SwagEdit is built. There is no fix for this yet, so we recommend that you disable the key binding for YEdit in Eclipse Preferences:
<img width="954" alt="yedit_disableformat" src="https://cloud.githubusercontent.com/assets/644582/13615520/b0411e3a-e543-11e5-93d7-dd4917be20da.png">

1. Open the "General > Keys" page.
2. Type "format" in the search box.
3. Select the command from the YEdit category.
4. Click the "Unbind Command" button.
5. Confirm by selecting "OK".
