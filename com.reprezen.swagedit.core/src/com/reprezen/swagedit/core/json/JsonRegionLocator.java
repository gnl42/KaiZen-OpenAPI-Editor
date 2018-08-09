package com.reprezen.swagedit.core.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.json.JsonRegion.Location;

public class JsonRegionLocator {

    private final Map<JsonPointer, JsonRegion> regions;
    private final Map<JsonPointer, Set<JsonPointer>> paths;
    private final JsonRegion root;

    public JsonRegionLocator(Map<JsonPointer, JsonRegion> regions, Map<JsonPointer, Set<JsonPointer>> paths) {
        this.regions = regions;
        this.paths = paths;
        this.root = buildRangeTreeHelper(JsonPointer.compile(""));
    }

    private JsonRegion buildRangeTreeHelper(JsonPointer pointer) {
        JsonRegion range = regions.get(pointer);
        if (range != null) {
            Set<JsonPointer> pointers = paths.get(pointer);
            if (pointers != null) {
                for (JsonPointer p : pointers) {
                    if (!p.equals(pointer)) {
                        JsonRegion n = regions.get(p);
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

    public JsonRegion findRegion(int line, int column) {
        if (column <= 1) {
            return root;
        }

        JsonRegion found = findContainingRegion(root.getChildren(), line, column);
        if (found == null) {
            found = findBeforeLine(root, line);
        }
        return found;
    }

    private JsonRegion findBeforeLine(JsonRegion container, int line) {
        JsonRegion found = null;
        int previousLine = 0;
        for (JsonRegion node : container.getChildren()) {
            int l = node.getContentLocation().startLine;
            if (l <= line && previousLine < l) {
                found = node;
                previousLine = l;
            }
        }
        return found;
    }

    private JsonRegion findContainingRegion(Collection<JsonRegion> ranges, int line, int column) {
        JsonRegion contain = null;
        Iterator<JsonRegion> it = ranges.iterator();
        while (it.hasNext() && contain == null) {
            JsonRegion current = it.next();
            if (isInside(current, line)) {
                if (column == current.getContentLocation().startColumn) {
                    contain = current;
                } else {
                    JsonRegion inside = findContainingRegion(current.getChildren(), line, column);
                    if (inside != null) {
                        contain = inside;
                    } else {
                        if (column > current.getContentLocation().startColumn && !current.getChildren().isEmpty()) {
                            JsonRegion lastBeforeLine = findBeforeLine(current, line);
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

    private boolean isInside(JsonRegion range, int line) {
        Location start;
        if (range.getFieldLocation() != null) {
            start = range.getFieldLocation();
        } else {
            start = range.getContentLocation();
        }

        return start.startLine <= line && line <= range.getContentLocation().endLine;
    }

    public JsonRegion get(JsonPointer pointer) {
        return regions.get(pointer);
    }

}