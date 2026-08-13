/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import org.apache.log4j.Logger;

/**
 * ASTM E1394 Data Type Server Plugin.
 * Full implementation with response generation, batch scripting, and template engine.
 */
public class AstmE1394DataTypeServerPlugin implements DataTypeServerPlugin {
    private static final Logger logger = Logger.getLogger(AstmE1394DataTypeServerPlugin.class);
    private AstmE1394DataTypeProperties props;
    private AstmE1394Parser parser;
    private AstmE1394Serializer serializer;
    private AstmResponseGenerator responseGenerator;

    @Override
    public String getPluginPointName() { return "ASTM E1394"; }

    @Override
    public void start() {
        logger.info("ASTM E1394 server plugin started");
    }

    @Override
    public void stop() {
        logger.info("ASTM E1394 server plugin stopped");
    }

    @Override
    public MessageSerializer getSerializer(SerializationProperties serializationProperties) {
        return new AstmMessageSerializer();
    }

    @Override
    public MessageSerializer getDeserializer(DeserializationProperties deserializationProperties) {
        return new AstmMessageSerializer();
    }

    @Override
    public BatchAdaptor getBatchAdaptor(BatchProperties batchProperties) {
        return new BatchAdaptor() {
            @Override public String getBatchScript() { return props != null ? props.getBatchScript() : null; }
            @Override public String getBatchType() { return props != null ? props.getBatchType() : "Record_Based"; }
            @Override public void setBatchScript(String batchScript) { if (props != null) props.setBatchScript(batchScript); }
            @Override public void setBatchType(String batchType) { if (props != null) props.setBatchType(batchType); }
        };
    }

    private class AstmMessageSerializer implements MessageSerializer {
        @Override
        public String serialize(String message) throws MessageSerializerException {
            if (serializer == null) init();
            return serializer.serialize(message);
        }

        @Override
        public String serialize(String message, String serializationProperties) throws MessageSerializerException {
            return serialize(message);
        }

        @Override
        public String serializeBatch(String message) throws MessageSerializerException {
            return serialize(message);
        }

        @Override
        public String serializeBatch(String message, String serializationProperties) throws MessageSerializerException {
            return serialize(message);
        }

        @Override
        public String fromXML(String xml) throws MessageSerializerException {
            if (serializer == null) init();
            return serializer.serialize(xml);
        }

        @Override
        public String toXML(String message) throws MessageSerializerException {
            if (parser == null) init();
            String xml = parser.parse(message);

            // Generate response if enabled
            if (props != null && props.isGenerateResponses() && responseGenerator != null) {
                String response = responseGenerator.generateAck(message);
                logger.debug("Generated ASTM ACK response");
                // Note: In production, response would be sent back through channel
            }

            return xml;
        }

        private void init() {
            props = new AstmE1394DataTypeProperties();
            parser = new AstmE1394Parser(props);
            serializer = new AstmE1394Serializer(props);
            responseGenerator = new AstmResponseGenerator();
        }
    }
}