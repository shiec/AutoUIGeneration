/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package shiec.secondBlock;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events. The <code>FileGenerator</code> is cacheable.
 *
 * @cocoon.sitemap.component.documentation
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events. The <code>FileGenerator</code> is cacheable.
 * @cocoon.sitemap.component.name   file
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Uses the last modification date of the xml document for validation
 *
 * @version $Id: FileGenerator.java 605687 2007-12-19 20:27:35Z vgritsenko $
 */
public class DevInfoGenerator extends ServiceableGenerator
                           implements CacheableProcessingComponent {

    /** The input source */
    protected Source inputSource;

    /** The SAX Parser. */
    protected SAXParser parser;


    public void setParser(SAXParser parser) {
        this.parser = parser;
    }

	/**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (this.inputSource != null) {
            super.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        if (this.parser != null) {
            super.manager.release(this.parser);
            this.parser = null;
        }
        super.recycle();
    }

    /**
     * Setup the file generator.
     * Try to get the last modification date of the source for caching.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        try {
        	// Lookup parser in Avalon contexts
        	if (this.parser == null) {
				this.parser = (SAXParser) this.manager.lookup(SAXParser.class.getName());
            }
        } catch (ServiceException e) {
            throw new ProcessingException("Exception when getting parser.", e);
        }

        try {
            this.inputSource = super.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Source " + super.source +
                              " resolved to " + this.inputSource.getURI());
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return this.inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            Document xmlDoc = SourceUtil.toDOM(super.manager, this.inputSource);
            Element toAdd = xmlDoc.createElement("statedata");
            Node stateElem = xmlDoc.getElementsByTagName("state").item(0);
            
            NodeList varList = ((Element)stateElem).getElementsByTagName("variable");
            
            for(int i=0;i<varList.getLength(); i++) {
            	Element node = (Element)varList.item(i);
            	Element var = xmlDoc.createElement("variable");
            	var.setAttribute("name", node.getAttribute("name"));
            	var.setTextContent("N/A");
            	toAdd.appendChild(var);
            }
                        
            xmlDoc.getFirstChild().appendChild(toAdd);
            DOMStreamer ds = new DOMStreamer(this.contentHandler);
            ds.stream(xmlDoc);
            
        } catch (SAXException e) {
            SourceUtil.handleSAXException(this.inputSource.getURI(), e);
        }
    }
}
