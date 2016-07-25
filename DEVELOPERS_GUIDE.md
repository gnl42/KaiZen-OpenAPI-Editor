#Developer's Guide

# Building SwagEdit from sources
Development of SwagEdit should be done with Eclipse. This project uses Maven/Tycho so you should have the Eclipse maven plugin `m2e` already installed.

## 1. Get sources
To start developing SwagEdit, clone the repository with the following command:

```
git clone git@github.com:ModelSolv/SwagEdit.git
``` 

## 2. Import to Eclipse IDE
Open Eclipse and select `File > Import... > Maven > Existing Maven Project` and select the folder SwagEdit.
This will put the project SwagEdit into your current workspace. 

## 3. Launch: From development Eclipse as Eclipse Application
From your Eclipse IDE with SwagEdit sources, open the "Launch configurations..." dialog, then right-click on the "Eclipse Application" located in the left side and choose "New". 

Make sure that SwagEdit plugins are included to the configuration, for example, you can select the "All workspace and enabled target plag-ins" in the "Plug-ins" tab:

## 3. Launch: using a local update site built from sources

From inside your SwagEdit folder, run the following command:

```
mvn clean verify
```

This command will build the project and generate an update site under the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository`.

You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site.
Click on `Local...` and select the folder `SwagEdit/com.reprezen.swagedit.repository/target/repository` and then click `Ok`.

You can now select and install SwagEdit from the Eclipse update manager.

# Style Guide
Please import [RepreZen Java Code Formatter](https://raw.githubusercontent.com/RepreZen/SwagEdit/master/etc/dev-env/ModSquad_formatter_profile.xml) to your Eclipse IDE.

# SwagEdit Architecture
![SwagEdit Architecture](https://cloud.githubusercontent.com/assets/644582/13757221/cf31b4e8-e9f9-11e5-8e6b-8aeb26fc3ac9.png)
