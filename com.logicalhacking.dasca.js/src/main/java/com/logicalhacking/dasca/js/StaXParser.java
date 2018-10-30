/*
 * Copyright (c) 2010-2015 SAP SE.
 *               2016-2018 The University of Sheffield.
 * 
 * All rights reserved. This program and the accompanying materials
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.logicalhacking.dasca.js;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class StaXParser {
    static final String ID = "id";
    static final String NAME = "name";
    static final String INPUT = "input";
    static final String EXP1 = "exp1";
    static final String EXP2 = "exp2";
    static final String EXP3 = "exp3";

    @SuppressWarnings({"unchecked"})
    public List<Item> readConfig(String configFile) {
        List<Item> items = new ArrayList<Item>();
        try {

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            Item item = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();

                    if (startElement.getName().getLocalPart() == (NAME)) {

                        item = new Item();

                        Iterator<Attribute> attributes = startElement
                                                         .getAttributes();

                        while (attributes.hasNext()) {

                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(ID)) {

                                item.setid(attribute.getValue());

                            }

                        }
                    }
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(INPUT)) {

                            event = eventReader.nextEvent();
                            item.setInput(event.asCharacters().getData());
                            continue;
                        }
                    }

                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(EXP1)) {

                            event = eventReader.nextEvent();
                            item.setExp1(event.asCharacters().getData());
                            continue;
                        }
                    }
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(EXP2)) {
                        event = eventReader.nextEvent();

                        item.setExp2(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(EXP3)) {
                        event = eventReader.nextEvent();

                        item.setExp3(event.asCharacters().getData());
                        continue;
                    }
                }

                if (event.isEndElement()) {

                    EndElement endElement = event.asEndElement();

                    if (endElement.getName().getLocalPart() == (NAME)) {
                        items.add(item);
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return items;
    }

}
