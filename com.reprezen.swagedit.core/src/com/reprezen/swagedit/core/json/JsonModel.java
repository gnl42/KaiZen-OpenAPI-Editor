package com.reprezen.swagedit.core.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
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
import com.reprezen.swagedit.core.json.RangeNode.Location;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class JsonModel {

    private final static LineRecorderYamlFactory factory = new LineRecorderYamlFactory();
    private final static ObjectMapper mapper = new ObjectMapper(factory);

    private final List<Exception> errors = Lists.newArrayList();
    private Map<JsonPointer, RangeNode> ranges = Maps.newHashMap();
    private Map<JsonPointer, Set<JsonPointer>> paths = Maps.newHashMap();
    private final Map<JsonPointer, TypeDefinition> types = Maps.newHashMap();
    private final Set<JsonPointer> references = Sets.newHashSet();

    private final CompositeSchema schema;
    private RangeNode range = null;
    private JsonNode content = null;

    public JsonModel(CompositeSchema schema, String text, boolean strict) throws IOException {
        this.schema = schema;

        if (Strings.emptyToNull(text) == null) {
            content = mapper.createObjectNode();
            ranges.put(JsonPointer.compile(""), new RangeNode(JsonPointer.compile("")));
            paths.put(JsonPointer.compile(""), Sets.<JsonPointer> newHashSet());
            range = buildRangeTree();
        } else {
            LineRecorderYamlParser parser = (LineRecorderYamlParser) factory.createParser(text);

            content = mapper.reader().readTree(parser);
            ranges = parser.getLines();
            paths = parser.getPaths();
            range = buildRangeTree();
        }
    }

    public List<Exception> getErrors() {
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

    private RangeNode buildRangeTree() {
        return buildRangeTreeHelper(JsonPointer.compile(""));
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

    private RangeNode buildRangeTreeHelper(JsonPointer pointer) {
        addTypeAndReferences(pointer);

        RangeNode range = ranges.get(pointer);
        if (range != null) {
            Set<JsonPointer> pointers = paths.get(pointer);
            if (pointers != null) {
                for (JsonPointer p : pointers) {
                    if (!p.equals(pointer)) {
                        RangeNode n = ranges.get(p);
                        if (n != null) {
                            range.getChildren().add(n);
                        }
                        buildRangeTreeHelper(p);
                    }
                }
            }

        }
        return range;
    }

    public RangeNode findRegion(int line, int column) {
        if (column <= 1) {
            return range;
        }

        RangeNode found = findContainingRegion(range.getChildren(), line, column);
        if (found == null) {
            found = findBeforeLine(range, line);
        }
        return found;
    }

    private RangeNode findBeforeLine(RangeNode container, int line) {
        RangeNode found = null;
        int previousLine = 0;
        for (RangeNode node : container.getChildren()) {
            int l = node.getContentLocation().startLine;
            if (l <= line && previousLine < l) {
                found = node;
                previousLine = l;
            }
        }
        return found;
    }

    private RangeNode findContainingRegion(Collection<RangeNode> ranges, int line, int column) {
        RangeNode contain = null;
        Iterator<RangeNode> it = ranges.iterator();
        while (it.hasNext() && contain == null) {
            RangeNode current = it.next();
            if (isInside(current, line)) {
                if (column == current.getContentLocation().startColumn) {
                    contain = current;
                } else {
                    RangeNode inside = findContainingRegion(current.getChildren(), line, column);
                    if (inside != null) {
                        contain = inside;
                    } else {
                        if (column > current.getContentLocation().startColumn && !current.getChildren().isEmpty()) {
                            RangeNode lastBeforeLine = findBeforeLine(current, line);
                            if (lastBeforeLine != null) {
                                contain = lastBeforeLine;
                            }
                        } else {
                            contain = current;
                        }
                    }
                }
            }
        }
        return contain;
    }

    private boolean isInside(RangeNode range, int line) {
        Location start;
        if (range.getFieldLocation() != null) {
            start = range.getFieldLocation();
        } else {
            start = range.getContentLocation();
        }

        return start.startLine <= line && line <= range.getContentLocation().endLine;
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

    public Map<JsonPointer, RangeNode> getRanges() {
        return ranges;
    }

}
