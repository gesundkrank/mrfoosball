/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.kicker.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

public class JsonConverter {

    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public JsonConverter(final Class<?>... classes) throws IOException {
        try {
            final var context = getContext(classes);
            this.marshaller = getJsonMarshaller(context);
            this.unmarshaller = getJsonUnmarshaller(context);
        } catch (final JAXBException e) {
            throw new IOException("Failed to init JsonConverter.", e);
        }
    }

    public <T> T fromString(final String json, Class<T> clazz) throws IOException {
        final var reader = new StringReader(json);
        try {
            return this.unmarshaller.unmarshal(new StreamSource(reader), clazz).getValue();
        } catch (JAXBException e) {
            throw new IOException("Failed to unmarshal json string.", e);
        }
    }

    public <T> String toString(T t) throws IOException {
        final var stringWriter = new StringWriter();
        try {
            this.marshaller.marshal(t, stringWriter);
        } catch (JAXBException e) {
            throw new IOException("Failed to marshall json object.", e);
        }
        return stringWriter.toString();
    }

    private static JAXBContext getContext(final Class<?>... classes) throws JAXBException {
        return JAXBContext.newInstance(classes);
    }

    private static Marshaller getJsonMarshaller(final JAXBContext context) throws JAXBException {
        final var marshaller = context.createMarshaller();
        marshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
        return marshaller;
    }

    private static Unmarshaller getJsonUnmarshaller(final JAXBContext context)
            throws JAXBException {
        final var unmarshaller = context.createUnmarshaller();
        unmarshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        unmarshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
        return unmarshaller;
    }
}
