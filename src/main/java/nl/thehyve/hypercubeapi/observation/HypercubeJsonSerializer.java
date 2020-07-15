/* (c) Copyright 2017, tranSMART Foundation, Inc. */

package nl.thehyve.hypercubeapi.observation;

import com.google.gson.stream.JsonWriter;
import nl.thehyve.hypercubeapi.query.dimension.*;
import nl.thehyve.hypercubeapi.query.hypercube.*;
import org.transmartproject.common.dto.Field;
import org.transmartproject.common.type.*;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <code>
 *                            ...,.,:,;lokxcl:;
 *                       .;ll:lOk,..    ..  .l0k:
 *                     .cd,cdo;;;c          .d:OO.
 *                     lc:::,..,.           ox;.
 *                    :l'ldc.',             xk.
 *                    o',x:.  ...           .,o:cl
 *                   ll.'l::'                    :c
 *                   x';,,;,. .'               .,;k'
 *                  'x  .'.   ,     ...  ,ll;lk0ck0K    .;::c.
 *                  ld  .   .,:   ;dx0o;ookXKkcc;,x0. .ooooxd;
 *                  l,     .lN:   :KXK00X0dc.lckOOkKlldxOc'.
 *                  oO.     cO0.  'kdcdKK,     'c.'odOd;
 *                   k,     ;Ooo .:x00Kc        ;;;Ox
 *                   .:o.   .x';;:Kxxdc.        lKk;
 *                     .x'   'o .kkOx; .'       cKl
 *                      :c.   0. l,'::  .,,. ..,'x.
 *                      :l,  .Nc;kclk;    .l,'kk.
 *                      ;l. .;O,0ON0d.       .;o
 *                      :, .'ddoXdol'       ...d.
 *                      .:.l,''ckX;d.          c:.....'c:;ccc:;:,.
 *                       oco:. ,kdc.           .,,,.....        .ol
 *                      .kkc;c0O.                                 .x.
 *                   .co:.;o0Oc,..                                  k.
 *                  .0c.,dkkd.    .''.                              :l
 *                .cc:lO:x0c         ..''                    .'.    :d
 *           cxold; oo' ;Oo.            .::;.               .llx:   ld
 *          o:.:d0oko   ,o., ,             .';,.          '.:: .k   lk
 *        'l: ..o0cdo.  .c.;;..                ,:.       ..    .x    0;
 *       dKocxdkxO,  ,   .dx..                   .,'.        . .d.   lk
 *     .kx::OXk0k,  .:     ;  '.                    ';,.    ,: ck.    X'
 *      klkKo0l    ;':.    ..                         .;,   ;' ,l     kc
 * </code>
 *
 * (original by Bertel Thorvaldsen - Image:ThorvaldsensJason.jpg, Public Domain,
 * https://commons.wikimedia.org/w/index.php?curid=4659064.)
 *
 * Serializes a {@link Hypercube} to JSON format.
 */
public class HypercubeJsonSerializer {

    /**
     * Contains information about an observation.
     */
    static class Cell {
        /**
         * The list of inlined values of this observation. This may contain nulls
         */
        List<Object> inlineDimensions = new ArrayList<>();
        /**
         * The list of indexes of indexed values of this observation. This may contain nulls.
         */
        List<Integer> dimensionIndexes = new ArrayList<>();
        /**
         * The numeric value of this observation if it is numeric.
         */
        Number numericValue;
        /**
         * The string value of this observation if it is of type text.
         */
        String stringValue;
    }

    protected Hypercube cube;
    protected JsonWriter writer;

    /**
     * Creates a hypercube serializer.
     *
     * @param cube the hypercube to serialize.
     * @param out the stream to write to.
     */
    HypercubeJsonSerializer(Hypercube cube, OutputStream out) {
        this.cube = cube;
        this.writer = new JsonWriter(new BufferedWriter(
            new OutputStreamWriter(out),
            // large 32k chars buffer to reduce overhead
            32*1024));
    }

    /**
     * Begins the output message.
     */
    protected void begin() throws IOException {
        writer.beginObject();
    }

    /**
     * Ends the output message.
     */
    protected void end() throws IOException {
        writer.endObject();
        writer.flush();
    }

    /**
     * Build an dimensional object to serialize using the field descriptions of the dimension.
     * @param dim the dimension to serialize the object for.
     * @param dimElement the value to serialize.
     * @return an object to use for writing.
     */
    protected static Object buildDimensionElement(Dimension dim, Object dimElement) {
        if (dimElement == null) {
            return null;
        }
        if (dim.isElementsSerializable()) {
            return dimElement;
        } else {
            Map<String, Object> value = new HashMap<>();
            for (Object prop: dim.getElementFields()) {
                Property property = (Property)prop;
                value.put(property.getName(), property.get(dimElement));
            }
            return value;
        }
    }

    /**
     * Create a cell with either numeric or string value
     * and inlined values for the inlined dimensions and indexes for the other dimensions.
     * @param value the hypercube value.
     * @return the cell representing the serialised value.
     */
    protected Cell createCell(HypercubeValue value) {
        Cell cell = new Cell();
        if (value.getValue() != null) {
            if (value.getValue() instanceof Number) {
                cell.numericValue = (Double) value.getValue();
            } else if (value.getValue() instanceof Date) {
                cell.stringValue = Instant.ofEpochMilli(((Date) value.getValue()).getTime()).toString();
            } else {
                cell.stringValue = value.getValue().toString();
            }
        }
        for (Dimension dim : cube.getDimensions()) {
            if (!dim.getDensity().isDense()) {
                // Add the value element inline
                cell.inlineDimensions.add(buildDimensionElement(dim, value.getAt(dim)));
            } else {
                // Add index to footer element inline. This may be null.
                cell.dimensionIndexes.add(value.getDimElementIndex(dim));
            }
        }
        return cell;
    }

    protected void writeValue(Object value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else if (value instanceof String) {
            writer.value((String) value);
        } else if (value instanceof Date) {
            String time = Instant.ofEpochMilli(((Date) value).getTime()).toString();
            writer.value(time);
        } else if (value instanceof Number) {
            writer.value((Number) value);
        } else if (value instanceof Map) {
            Map<String, Object> obj = (Map<String, Object>) value;
            writer.beginObject();
            for (Map.Entry e: obj.entrySet()) {
                writer.name((String) e.getKey());
                writeValue(e.getValue());
            }
            writer.endObject();
        } else {
            writer.value(value.toString());
        }
    }

    protected void writeCell(Cell cell) throws IOException {
        writer.beginObject();
        writer.name("inlineDimensions");
        writer.beginArray();
        for(Object dimension: cell.inlineDimensions) {
            writeValue(dimension);
        }
        writer.endArray();
        writer.name("dimensionIndexes");
        writer.beginArray();
        for(Integer index : cell.dimensionIndexes) {
            writer.value(index);
        }
        writer.endArray();
        if (cell.numericValue != null) {
            writer.name("numericValue").value(cell.numericValue);
        } else if (cell.stringValue != null) {
            writer.name("stringValue").value(cell.stringValue);
        }
        writer.endObject();
    }

    /**
     * Writes the sequence of messages representing the values passed by the
     * value iterator.
     */
    protected void writeCells() throws IOException {
        Iterator<HypercubeValue> it = cube.iterator();
        writer.name("cells");
        writer.beginArray();
        while (it.hasNext()) {
            Cell message = createCell(it.next());
            writeCell(message);
        }
        writer.endArray();
    }

    /**
     * Build the list of {@link DimensionProperties} to be serialized in the header.
     * @return a list of dimension declarations.
     */
    protected List<DimensionProperties> buildDimensionDeclarations() {
        return cube.getDimensions().stream()
            .map(DimensionPropertiesMapper::forDimension)
            .collect(Collectors.toList());
    }

    protected void writeField(Field field) throws IOException {
        writer.beginObject();
        writer.name("name").value(field.getName());
        writer.name("type").value(field.getType().name());
        writer.endObject();
    }

    protected void writeDimensionProperties(DimensionProperties dimension) throws IOException {
        writer.beginObject();
        writer.name("name").value(dimension.name);
        writer.name("dimensionType").value(
            dimension.getDimensionType() == null ? null : dimension.getDimensionType().name().toLowerCase());
        writer.name("sortIndex").value(dimension.sortIndex);
        writer.name("valueType").value(
            dimension.getValueType() == null ? ValueType.Object.name() : dimension.getValueType().name());
        if (dimension.getModifierCode() != null) {
            writer.name("modifierCode").value(dimension.getModifierCode());
        }
        if (dimension.getFields() != null) {
            writer.name("fields");
            writer.beginArray();
            for(Field f : dimension.getFields()) {
                writeField(f);
            }
            writer.endArray();
        }
        if (dimension.inline) {
            writer.name("inline").value(true);
        }
        writer.endObject();
    }

    /**
     * Writes a header message describing the dimensions of the value messages that
     * will be written.
     */
    protected void writeHeader() throws IOException {
        writer.name("dimensionDeclarations");
        writer.beginArray();
        for(DimensionProperties props : buildDimensionDeclarations()) {
            writeDimensionProperties(props);
        }
        writer.endArray();

        writer.name("sort");
        writer.beginArray();
        for(Map.Entry<Dimension, SortOrder> entry: cube.getSortOrder().entrySet()) {
            writer.beginObject();
            writer.name("dimension").value(entry.getKey().getName());
            writer.name("sortOrder").value(entry.getValue().name().toLowerCase());
            writer.endObject();
        }
        writer.endArray();
    }

    /**
     * Writes a footer message containing the indexed dimension elements referred to in the value
     * messages.
     */
    protected void writeFooter() throws IOException {
        writer.name("dimensionElements");
        writer.beginObject();
        for(Dimension dim: cube.getDimensions()) {
            if (!dim.getDensity().isDense()) {
                continue;
            }
            writer.name(dim.getName());
            writer.beginArray();
            for(Object element: cube.dimensionElements(dim)) {
                writeValue(buildDimensionElement(dim, element));
            }
            writer.endArray();
        }
        writer.endObject();
    }

    /**
     * Writes a message or sequence of messages serializing the data in the hybercube
     * {@link #cube}.
     * First the header is written ({@link #writeHeader}, then the cells serializing
     * the values in the cube ({@link #writeCells}), then the footer containing referenced objects
     * (@link #writeFooter).
     */
    void write() throws IOException {
        begin();
        writeHeader();
        writeCells();
        writeFooter();
        end();
    }
}
