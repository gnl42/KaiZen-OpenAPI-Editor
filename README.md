SwagEdit
---

Eclipse Editor for Swagger API Description Language.

# Development

Development of SwagEdit should be done with Eclipse. This project uses Maven/Tycho so you should have the Eclipse maven plugin `m2e` 
already installed.

To start developing SwagEdit, clone the repository with the following command:

```
git clone git@github.com:ModelSolv/SwagEdit.git
``` 

Open Eclipse and select `File > Import... > Maven > Existing Maven Project` and select the folder SwagEdit.
This will put the project SwagEdit into your current workspace. 

# Install RepreZen
## From http://products.modelsolv.com/swagedit/kepler/
You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site. Use "http://products.modelsolv.com/swagedit/kepler/" as URL.

## Build a local update site

From inside your SwagEdit folder, run the following command:

```
mvn clean verify
```

This command will build the project and generate an update site under the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository`.

You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site.
Click on `Local...` and select the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository` and then click `Ok`.

You can now select and install SwagEdit from the Eclipse update manager.



