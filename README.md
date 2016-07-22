# SwagEdit - Eclipse Editor for Swagger–OpenAPI

SwagEdit is an Eclipse Editor for the [Swagger](http://swagger.io) API Description Language, now known as the [OpenAPI Specification](http://openapis.org).

SwagEdit was developed for [RepreZen API Studio](http://reprezen.com/swagger-tools), a comprehensive solution for API modeling, documentation, visualization, testing and code generation, built on Eclipse. Swagger–OpenAPI.

We welcome your suggestions and contributions!

## Watch a short video about SwagEdit
[![Editing Swagger-OpenAPI in RepreZen API Studio](http://img.youtube.com/vi/KX_tHp_KQkE/0.jpg)](https://www.youtube.com/watch?v=KX_tHp_KQkE)

## Install SwagEdit
### Use a published update site 
You can now install SwagEdit into your Eclipse by clicking on `Help > Install New Software... > Add...`
This will show a dialog box from where you can select the location of the update site.
Use the update site from [http://products.modelsolv.com/swagedit/0.1.0/latest/](http://products.modelsolv.com/swagedit/0.1.0/latest/) as URL.

## User Documentation
Read more about SwagEdit features on http://www.reprezen.com/swagger-tools

## Troubleshooting
### Autoformat (Ctrl+Shift+F) removes YAML comments 
This is a [known issue](https://github.com/oyse/yedit/issues/15) of YEdit, the YAML Editor on which SwagEdit is built. There is no fix for this yet, so we recommend that you disable the key binding for YEdit in Eclipse Preferences:
<img width="954" alt="yedit_disableformat" src="https://cloud.githubusercontent.com/assets/644582/13615520/b0411e3a-e543-11e5-93d7-dd4917be20da.png">

1. Open the "General > Keys" page.
2. Type "format" in the search box.
3. Select the command from the YEdit category.
4. Click the "Unbind Command" button.
5. Confirm by selecting "OK".

## Development
Please see https://github.com/RepreZen/SwagEdit/blob/master/DEVELOPERS_GUIDE.md

## License
SwagEdit is provided under the Eclipse Public License (https://www.eclipse.org/legal/epl-v10.html)

