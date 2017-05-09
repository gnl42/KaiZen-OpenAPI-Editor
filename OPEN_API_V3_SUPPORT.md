# Open API v3 Support (*Experimental*)
We are really excited to announce that KaiZen OpenAPI Editor now supports [OpenAPI v3](https://github.com/OAI/OpenAPI-Specification/blob/OpenAPI.next/versions/3.0.md). The new feature is still under active development, but it already allows you to do really cool stuff with OpenAPI v3 specs.

## Validation
<img src="http://i.imgur.com/WSns2FY.png" alt="Validation: invalid 'kind' property" width="500"/></a>

## Content Assist
<img src="http://i.imgur.com/TwZ31hg.png" alt="Content Assist"  width="300"/></a>

## Quick Outline
<img src="http://i.imgur.com/l6ADPcs.png" alt="Quick Outline" width="400"/></a>

## Outline
<img src="http://i.imgur.com/44aOeuh.png" alt="Outline" width="300"/></a>

# FAQ
## How to create a new OpenAPI v3 spec?
Please use the *OpenAPI v3 Spec* wizard from the *KaiZen OpenAPI Editor* category:


<img src="http://i.imgur.com/pTOQH8z.png" alt="File>New>Other" width="300">  <img src="http://i.imgur.com/4uKKvF0.png" alt="KaiZen OpenAPI Editor Category" width="400">

## Editor Swagger v2 uses an underlying JSON Schema. There is no official JSON Schema for OpenAPI v3, so what are you using?
We use an modification of the JSON Schema provided by [googleapis/gnostic](https://github.com/googleapis/gnostic). We use the [SwagEdit branch](https://github.com/RepreZen/gnostic/tree/SwagEdit) to generate our JSON Schema.
