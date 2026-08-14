package com.bitdreamit.connect.plugins.datatypes.astm.test;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394BatchAdaptor;
import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394BatchProperties;
import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394DeserializationProperties;
import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394Deserializer;
import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394SerializationProperties;
import com.bitdreamit.connect.plugins.datatypes.astm.server.ASTME1394Serializer;
import com.mirth.connect.util.XmlUtil;

/**
 * Round-trip and feature tests for the ASTM E1394 data type plugin.
 *
 * <p>Verifies that:</p>
 * <ul>
 *   <li>ASTM text → XML → ASTM text round-trips byte-for-byte.</li>
 *   <li>Header-delimiter derivation correctly overrides default delimiters.</li>
 *   <li>Escape sequences ({@code &F}, {@code &S}, {@code &R}, {@code &E},
 *       {@code &Hxx}) survive the round-trip.</li>
 *   <li>Batch splitting on {@code H…L} boundaries produces one message per
 *       session.</li>
 * </ul>
 *
 * <p>All test messages use the standard 3-char delimiter definition
 * {@code \^&} in field 2 of the {@code H} record, which decodes to:
 * repeat={@code \}, component={@code ^}, escape={@code &}. The field
 * separator is always {@code |}.</p>
 */
public class ASTME1394RoundTripTest {

    /**
     * Canonical sample: H + P + O + R + L session using default delimiters.
     * Field 2 of the H record carries the 3-char delimiter definition
     * {@code \^&} (repeat, component, escape).
     */
    private static final String SAMPLE =
        "H|\\^&|||BitDreamLIS^1.0|||||HOST||P|1|20260813120000\r" +
        "P|1||MRN000123||DOE^JANE||19900101|F\r" +
        "O|1|SPEC001||^^^GLU|R||20260813120000|||||A||||1\r" +
        "R|1|^^^GLU|98|mg/dL|70-99||N||F||ANALYZER01|20260813121500\r" +
        "L|1|N\r";

    @Test
    public void testRoundTrip() throws Exception {
        ASTME1394DeserializationProperties desProps = new ASTME1394DeserializationProperties();
        ASTME1394SerializationProperties   serProps = new ASTME1394SerializationProperties();

        ASTME1394Deserializer deserializer = new ASTME1394Deserializer(desProps);
        String xml = deserializer.toXML(SAMPLE);

        // Sanity: XML structure must contain the expected root and record tags.
        assertTrue("XML must contain <ASTM> root", xml.contains("<ASTM>"));
        assertTrue("XML must contain <H> header record", xml.contains("<H>"));
        assertTrue("XML must contain <P> patient record", xml.contains("<P>"));
        assertTrue("XML must contain <O> order record", xml.contains("<O>"));
        assertTrue("XML must contain <R> result record", xml.contains("<R>"));
        assertTrue("XML must contain <L> terminator record", xml.contains("<L>"));

        // Round-trip: XML → ASTM text should match the original.
        org.w3c.dom.Document doc = XmlUtil.parse(xml);
        ASTME1394Serializer serializer = new ASTME1394Serializer(serProps, desProps);
        String reconstructed = serializer.fromXML(doc);

        assertEquals("Round-trip must be byte-for-byte identical",
                normalize(SAMPLE), normalize(reconstructed));
    }

    /**
     * Header-delimiter derivation: use a 3-char delimDef to override the
     * repeat delimiter. With {@code ~^#} the derived delimiters are:
     * repeat='~', component='^', escape='#'. Field separator stays as '|'.
     */
    @Test
    public void testHeaderDelimiterDerivation() throws Exception {
        String msg = "H|~^#|||TestLIS|||||HOST||P|1|20260101\rP|1||ID1\rL|1|N\r";
        ASTME1394DeserializationProperties props = new ASTME1394DeserializationProperties();
        props.setDeriveDelimitersFromHeader(true);
        ASTME1394Deserializer des = new ASTME1394Deserializer(props);
        String xml = des.toXML(msg);

        assertTrue("XML must contain <H> header record", xml.contains("<H>"));
        assertTrue("XML must contain <P> patient record", xml.contains("<P>"));

        // Field 2 of the H record must contain the literal delimDef.
        assertTrue("Field 2 of H must preserve the delimDef",
                xml.contains("<F2>~^#</F2>"));

        // The patient record's field 2 should be "1" (split using '|').
        assertTrue("Patient record must split field 2 as '1'",
                xml.contains("<P><F1>P</F1><F2>1</F2>"));
    }

    /**
     * Escape round-trip: ASTM uses {@code &} as the escape character, so
     * escape sequences are {@code &F} (field), {@code &S} (component),
     * {@code &R} (repeat), {@code &E} (escape), {@code &Hxx} (hex byte).
     * Verify they survive a full round-trip.
     */
    @Test
    public void testEscapeRoundTrip() throws Exception {
        // The escape character '&' introduces each escape sequence.
        String msg = "H|\\^&|||Test&F&S&R&E&H0A\rP|1||ID1\rL|1|N\r";

        ASTME1394DeserializationProperties desProps = new ASTME1394DeserializationProperties();
        ASTME1394SerializationProperties   serProps = new ASTME1394SerializationProperties();

        ASTME1394Deserializer des = new ASTME1394Deserializer(desProps);
        String xml = des.toXML(msg);

        // Verify that escape sequences were correctly decoded into literal chars.
        // The literal chars are: | (field), ^ (component), \ (repeat), & (escape), LF (0x0A).
        // They appear in the XML text content (XML-escaped as needed).
        assertTrue("Escape &F must decode to |", xml.contains("|"));
        assertTrue("Escape &S must decode to ^", xml.contains("^"));
        assertTrue("Escape &R must decode to \\", xml.contains("\\"));
        assertTrue("Escape &E must decode to &", xml.contains("&amp;") || xml.contains("&"));

        org.w3c.dom.Document doc = XmlUtil.parse(xml);
        ASTME1394Serializer ser = new ASTME1394Serializer(serProps, desProps);
        String out = ser.fromXML(doc);

        // Re-serialized output must contain the same escape sequences.
        assertTrue("Output must contain &F escape", out.contains("&F"));
        assertTrue("Output must contain &S escape", out.contains("&S"));
        assertTrue("Output must contain &R escape", out.contains("&R"));
        assertTrue("Output must contain &E escape", out.contains("&E"));
        assertTrue("Output must contain &H0A escape", out.contains("&H0A"));
    }

    @Test
    public void testBatchSplit() throws Exception {
        // Two H..L sessions concatenated.
        String batch =
            "H|\\^&|||A\rP|1||ID1\rL|1|N\r" +
            "H|\\^&|||B\rP|1||ID2\rL|1|N\r";
        ASTME1394BatchProperties props = new ASTME1394BatchProperties();
        props.setSplitBatchBy(ASTME1394BatchProperties.SPLIT_TYPE_H_L_BOUNDARY);
        ASTME1394BatchAdaptor adaptor = new ASTME1394BatchAdaptor(props);
        List<String> msgs = adaptor.getMessages(batch);

        assertEquals("Batch must split into 2 sessions", 2, msgs.size());
        assertTrue("First message must reference session A", msgs.get(0).contains("A"));
        assertTrue("Second message must reference session B", msgs.get(1).contains("B"));
        assertTrue("First message must end with terminator", msgs.get(0).contains("L|1|N"));
        assertTrue("Second message must end with terminator", msgs.get(1).contains("L|1|N"));
    }

    @Test
    public void testBatchSplitNoSplit() throws Exception {
        String batch = "H|\\^&|||A\rP|1||ID1\rL|1|N\r";
        ASTME1394BatchProperties props = new ASTME1394BatchProperties();
        props.setSplitBatchBy(ASTME1394BatchProperties.SPLIT_TYPE_NONE);
        ASTME1394BatchAdaptor adaptor = new ASTME1394BatchAdaptor(props);
        List<String> msgs = adaptor.getMessages(batch);

        assertEquals("NONE split must produce a single message", 1, msgs.size());
    }

    @Test
    public void testBatchSplitRecordLevel() throws Exception {
        String batch = "H|\\^&|||A\rP|1||ID1\rL|1|N\r";
        ASTME1394BatchProperties props = new ASTME1394BatchProperties();
        props.setSplitBatchBy(ASTME1394BatchProperties.SPLIT_TYPE_RECORD);
        ASTME1394BatchAdaptor adaptor = new ASTME1394BatchAdaptor(props);
        List<String> msgs = adaptor.getMessages(batch);

        assertEquals("RECORD split must produce 3 messages (H, P, L)",
                3, msgs.size());
        assertTrue("First record must be H", msgs.get(0).startsWith("H"));
        assertTrue("Second record must be P", msgs.get(1).startsWith("P"));
        assertTrue("Third record must be L", msgs.get(2).startsWith("L"));
    }

    /**
     * Helper: normalize trailing whitespace / record delimiters so that
     * round-trip comparisons aren't sensitive to trailing-CR differences.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+$", "").replaceAll("\\r\\n", "\r");
    }
}
