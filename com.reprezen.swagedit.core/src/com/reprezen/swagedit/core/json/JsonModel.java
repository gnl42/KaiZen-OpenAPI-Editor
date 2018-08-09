package com.reprezen.swagedit.core.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import com.reprezen.swagedit.core.validation.SwaggerError;

public class JsonModel {

    private final static LineRecorderYamlFactory factory = new LineRecorderYamlFactory();
    private final static ObjectMapper mapper = new ObjectMapper(factory);

    private final List<SwaggerError> errors = Lists.newArrayList();
    private final Map<JsonPointer, TypeDefinition> types = Maps.newHashMap();
    private final Set<JsonPointer> references = Sets.newHashSet();

    private Map<JsonPointer, JsonRegion> regions = Maps.newHashMap();
    private Map<JsonPointer, Set<JsonPointer>> paths = Maps.newHashMap();

    private final CompositeSchema schema;
    private final JsonRegionLocator locator;

    private JsonRegion range = null;
    private JsonNode content = null;

    public JsonModel(CompositeSchema schema, String text, boolean strict) throws IOException {
        this.schema = schema;

        if (Strings.emptyToNull(text) == null) {
            this.content = mapper.createObjectNode();
            this.regions.put(JsonPointer.compile(""), new JsonRegion(JsonPointer.compile("")));
            this.paths.put(JsonPointer.compile(""), Sets.<JsonPointer> newHashSet());
        } else {
            LineRecorderYamlParser parser = (LineRecorderYamlParser) factory.createParser(text);
            this.content = mapper.reader().readTree(parser);
            this.regions = parser.getLines();
            this.paths = parser.getPaths();
            this.errors.addAll(parser.getErrors());
        }

        this.locator = new JsonRegionLocator(regions, paths);

        for (JsonPointer ptr : regions.keySet()) {
            addTypeAndReferences(ptr);
        }
    }

    public List<SwaggerError> getErrors() {
        return errors;
    }

    public CompositeSchema getSchema() {
        return schema;
    }

    public Set<JsonPointer> getReferences() {
        return references;
    }

    public JsonNode getContent() {
        return content;
    }

    private void addTypeAndReferences(JsonPointer ptr) {
        TypeDefinition type = schema.getType(ptr);
        if (type != null) {
            types.put(ptr, type);
        }

        if (ptr.toString().endsWith("$ref")) {
            references.add(ptr);
        }
    }

    public List<JsonNode> findByType(JsonPointer typePointer) {
        List<JsonNode> found = Lists.newArrayList();
        for (JsonPointer ptr : types.keySet()) {
            TypeDefinition type = types.get(ptr);
            if (type.getPointer().equals(typePointer)) {
                found.add(content.at(ptr));
            }
        }
        return found;
    }

    public Map<JsonPointer, TypeDefinition> getTypes() {
        return types;
    }

    /**
     * Returns the type definition if present for the node located at the given pointer.
     * 
     * @param ptr
     * @return type
     */
    public TypeDefinition getType(JsonPointer ptr) {
        return getTypes().get(ptr);
    }

    public Map<JsonPointer, JsonRegion> getRanges() {
        return regions;
    }

    public Map<JsonPointer, Set<JsonPointer>> getPaths() {
        return paths;
    }

    /**
     * Returns the closest region that contains the location represented by a line and column.
     * 
     * @param line
     * @param column
     * @return region
     */
    public JsonRegion findRegion(int line, int column) {
        return locator.findRegion(line, column);
    }

    /**
     * Returns the region of the given pointer.
     * 
     * @param pointer
     * @return region
     */
    public JsonRegion getRegion(JsonPointer pointer) {
        return pointer != null ? regions.get(pointer) : null;
    }

    public void setPath() {

    }

    public Object getPath() {
        // TODO Auto-generated method stub
        return null;
    }

}
