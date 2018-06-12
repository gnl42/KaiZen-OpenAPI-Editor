package com.reprezen.swagedit.core.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.json.RangeNode.Location;

public class LineRecorderYamlParser extends YAMLParser {
    private final Map<JsonPointer, RangeNode> ranges = Maps.newHashMap();
    private final Map<JsonPointer, Set<JsonPointer>> paths = Maps.newHashMap();

    private JsonPointer ptr = JsonPointer.compile("");
    private boolean seenRoot = false;
    private boolean strictMode;

    public LineRecorderYamlParser(IOContext ctxt, BufferRecycler br, int parserFeatures, int formatFeatures,
            ObjectCodec codec, Reader reader) {
        super(ctxt, br, parserFeatures, formatFeatures, codec, reader);
    }

    public Map<JsonPointer, RangeNode> getLines() {
        return ranges;
    }

    public Map<JsonPointer, Set<JsonPointer>> getPaths() {
        return paths;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        JsonToken token = null;
        try {
            token = super.nextToken();
        } catch (Exception e) {
            if (strictMode) {
                throw e;
            }
        }

        if (token != null) {
            processLineEntry(token, getCurrentLocation(), getParsingContext());
        }

        return token;
    }

    private void processLineEntry(JsonToken token, JsonLocation location, JsonStreamContext context) {
        /*
         * Root needs to be handled specially.
         */
        if (!seenRoot) {
            RangeNode range = getOrCreateRange(ptr);
            range.setContentLocation(new Location( //
                    location.getLineNr(), //
                    location.getColumnNr(), //
                    location.getLineNr(), //
                    location.getColumnNr()));

            ranges.put(ptr, range);
            paths.put(ptr, Sets.<JsonPointer> newHashSet());

            seenRoot = true;
            return;
        }

        /*
         * We get that if JSON Pointer "" points to a container... We need to skip that
         */
        if (context.inRoot()) {
            RangeNode range = ranges.get(ptr);
            Location previousLocation = range.getContentLocation();
            range.setContentLocation(new Location( //
                    previousLocation.startLine, //
                    previousLocation.startColumn, //
                    location.getLineNr(), //
                    location.getColumnNr()));

            return;
        }

        /*
         * If the end of a container, "pop" one level
         */
        if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
            RangeNode range = getOrCreateRange(ptr);
            Location previousLocation = range.getContentLocation();
            range.setContentLocation(new Location( //
                    previousLocation.startLine, //
                    previousLocation.startColumn, //
                    location.getLineNr(), //
                    location.getColumnNr()));

            ptr = ptr.head();

            return;
        }

        /*
         * This is not addressable...
         */
        if (token == JsonToken.FIELD_NAME) {
            Set<JsonPointer> list = paths.get(ptr.head());
            if (list == null) {
                list = Sets.newHashSet();
            }
            list.add(ptr);

            JsonPointer fieldPointer = append(ptr, context);

            RangeNode range = getOrCreateRange(fieldPointer);
            range.setFieldLocation(new Location( //
                    location.getLineNr(), //
                    location.getColumnNr(), //
                    location.getLineNr(), //
                    location.getColumnNr()));

            paths.put(ptr.head(), list);
            return;
        }

        final JsonStreamContext parent = context.getParent();

        /*
         * But this is; however we need to know what the parent is to do things correctly, delegate to another method
         */
        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            startContainer(parent, location);
            return;
        }

        /*
         * OK, "normal" entry, build the pointer
         */
        final JsonPointer entryPointer = append(ptr, context);

        RangeNode range = getOrCreateRange(entryPointer);
        range.setContentLocation(new Location( //
                location.getLineNr(), //
                location.getColumnNr(), //
                location.getLineNr(), //
                location.getColumnNr()));

        JsonPointer top = entryPointer.head();
        Set<JsonPointer> list = paths.get(top);
        if (list == null) {
            list = Sets.newHashSet();
        }
        list.add(entryPointer);
        paths.put(top, list);
    }

    protected RangeNode getOrCreateRange(JsonPointer pointer) {
        RangeNode range = ranges.get(pointer);
        if (range == null) {
            ranges.put(pointer, range = new RangeNode(pointer));
        }
        return range;
    }

    private void startContainer(final JsonStreamContext parent, JsonLocation location) {
        ptr = append(ptr, parent);

        RangeNode range = getOrCreateRange(ptr);
        range.setContentLocation(new Location( //
                location.getLineNr(), //
                location.getColumnNr(), //
                location.getLineNr(), //
                location.getColumnNr()));

        Set<JsonPointer> list = paths.get(ptr.head());
        if (list == null) {
            list = Sets.newHashSet();
        }
        list.add(ptr);
        paths.put(ptr.head(), list);
    }

    private JsonPointer append(JsonPointer ptr, JsonStreamContext context) {
        if (context.inArray())
            return ptr.append(JsonPointer.compile("/" + context.getCurrentIndex()));
        else if (context.inObject()) {
            return ptr.append(JsonPointer.compile("/" + context.getCurrentName().replaceAll("/", "~1")));
        }
        else
            return ptr;
    }

    public void setStrict(boolean strict) {
        this.strictMode = strict;
    }
}
