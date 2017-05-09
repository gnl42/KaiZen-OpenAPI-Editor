# KaiZen OpenAPI Editor - Developer's Guide

# Building from Sources
Development of KaiZen Editor should be done with Eclipse. This project uses Maven/Tycho so you should have the Eclipse maven plugin `m2e` already installed.

## 1. Get sources
To start developing, clone the repository with the following command:

```
git clone git@github.com:RepreZen/KaiZen-OpenAPI-Editor.git
``` 

## 2. Import to Eclipse IDE
Open Eclipse and select `File > Import... > Maven > Existing Maven Project`. Then select the folder KaiZen-OpenAPI-Editor.
This will add the project into your current workspace. 

## 3. Launch: From development Eclipse as Eclipse Application
From Eclipse with the KaiZen-OpenAPI-Editor source project, open the "Launch configurations..." dialog, then right-click on the "Eclipse Application" located in the left side and choose "New". 

Make sure that KaiZen-OpenAPI-Editor plugins are included to the configuration, for example, you can select the "All workspace and enabled target plag-ins" in the "Plug-ins" tab:

## 3. Launch: using a local update site built from sources

From inside your KaiZen-OpenAPI-Editor folder, run the following command:

```
mvn clean verify
```

This command will build the project and generate an update site under the folder `KaiZen-OpenAPI-Editor/com.reprezen.swagedit.repository/target/repository`.

You can now install KaiZen OpenAPI Editor into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box where you can select the location of the update site.
Click on `Local...` and select the folder `KaiZen-OpenAPI-Editor/com.reprezen.swagedit.repository/target/repository` and then click `Ok`.

You can now select and install KaiZen-OpenAPI-Editor from the Eclipse update manager.

# Style Guide
Please import [RepreZen Java Code Formatter](https://raw.githubusercontent.com/RepreZen/KaiZen-OpenAPI-Editor/master/etc/dev-env/ModSquad_formatter_profile.xml) to your Eclipse IDE.

# Architecture
![KaiZen OpenAPI Editor Architecture](https://cloud.githubusercontent.com/assets/644582/13757221/cf31b4e8-e9f9-11e5-8e6b-8aeb26fc3ac9.png)

# JSON Schema
Validation, code assist, outline, and quick outline are built based on JSON Schema which is analyzed by our model. You can find more details in [#198 KaiZen OpenAPI Editor Model](https://github.com/RepreZen/KaiZen-OpenAPI-Editor/issues/198).
![JSON Schema Model](http://i.imgur.com/h38zU2C.png)
### Validation
See `com.reprezen.swagedit.validation.Validator.isSchemaDefinition(AbstractNode)`
* Severity: warning vs error
* Message
* Line in the document

### Code assist: LabelProvider
See `com.reprezen.swagedit.assist.SwaggerProposalProvider.getProposals(TypeDefinition, AbstractNode, String)`
Given a documentOffset, we need:
* replacementString
* displayString
* description
* type (displayed after the “:”)

### Outline and QuickOutline: LabelProvider
See `com.reprezen.swagedit.editor.outline.OutlineStyledLabelProvider.getStyledString(AbstractNode)`
Labels are composed from the corresponding element in the JSON Schema, it’s based on the “title” property of the node, name of the containing property of the path to the property.
