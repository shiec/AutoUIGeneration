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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.util.PostInputStream;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * The <code>StreamGenerator</code> is a class that reads XML from a request
 * InputStream and generates SAX Events.
 *
 * <p>For the POST requests with a mimetype of <code>application/x-www-form-urlencoded</code>
 * or <code>multipart/form-data<code> the xml data is expected to be associated
 * with the sitemap parameter <code>form-name</code>.
 *
 * <p>For the POST requests with mimetypes <code>text/plain</code>, <code>text/xml</code>,
 * <code>application/xhtml+xml</code>, <code>application/xml</code> the xml data
 * is expected to be in the body of the POST request and its length is specified
 * by the value returned by {@link Request#getContentLength} method.
 *
 * <p>The StreamGenerator uses helper {@link PostInputStream} class for InputStream
 * reading operations. At the time when Parser is reading the data out of the
 * InputStream, Parser has no knowledge about the length of data to be read.
 * The only way to signal to the Parser that all data was read from the
 * InputStream is to control reading operation - by the means of
 * PostInputStream - and to return to the requestor '-1' when the
 * number of bytes read is equal to the content length value.
 *
 * @cocoon.sitemap.component.documentation
 * The <code>StreamGenerator</code> is a class that reads XML from a
 * request InputStream and generates SAX Events.
 * @cocoon.sitemap.component.name   stream
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching No
 * @cocoon.sitemap.component.pooling.max  16
 *
 * @version $Id: StreamGenerator.java 605579 2007-12-19 15:27:16Z vgritsenko $
 */
public class RegisterGenerator extends ServiceableGenerator {

    /** The parameter holding the name associated with the xml data  **/
    public static final String FORM_NAME = "form-name";

    /**
     * Generate XML data out of request InputStream.
     * @throws SAXException 
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
    	System.out.println("getting in...");
        SAXParser parser = null;
        int len = 0;
        String contentType;

        Request request = ObjectModelHelper.getRequest(this.objectModel);
        try {
            contentType = request.getContentType();
            
            if (contentType == null) {
                contentType = parameters.getParameter("defaultContentType", null);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("no Content-Type header - using contentType parameter: " + contentType);
                }
                if (contentType == null) {
                    throw new IOException("Both Content-Type header and defaultContentType parameter are not set");
                }
            }
            InputSource source;
            if (contentType.startsWith("application/x-www-form-urlencoded") ||
                    contentType.startsWith("multipart/form-data")) {
                String parameter = parameters.getParameter(FORM_NAME, null);
                if (parameter == null) {
                    throw new ProcessingException("StreamGenerator expects a sitemap parameter called '" +
                                                  FORM_NAME + "' for handling form data");
                }

                Object xmlObject = request.get(parameter);
                Reader xmlReader;
                if (xmlObject instanceof String) {
                    xmlReader = new StringReader((String) xmlObject);
                } else if (xmlObject instanceof Part) {
                    xmlReader = new InputStreamReader(((Part) xmlObject).getInputStream());
                } else {
                    throw new ProcessingException("Unknown request object encountered named " + 
                                                  parameter + " : " + xmlObject);
                }

                source = new InputSource(xmlReader);
            } else if (contentType.startsWith("text/plain") ||
                    contentType.startsWith("text/xml") ||
                    contentType.startsWith("application/xhtml+xml") ||
                    contentType.startsWith("application/xml")) {

                HttpServletRequest httpRequest = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
                if (httpRequest == null) {
                    throw new ProcessingException("This feature is only available in an http environment.");
                }
                len = request.getContentLength();
                if (len <= 0) {
                    throw new IOException("getContentLen() == 0");
                }

                PostInputStream anStream = new PostInputStream(httpRequest.getInputStream(), len);
                source = new InputSource(anStream);
            } else {
                throw new IOException("Unexpected getContentType(): " + request.getContentType());
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Processing stream ContentType=" + contentType + " ContentLength=" + len);
            }
            String charset = getCharacterEncoding(request, contentType);
            if (charset != null) {
                source.setEncoding(charset);
            }
            
            String path = "D:/Java/kepler/workspace/secondBlock/src/main/resources/COB-INF/database/";
            //String path = "../database/";
        	File file = new File(path+"description.xml");
        	if(!file.exists()) {
    		    try {
    				file.createNewFile();
    				System.out.println("creating new description file...");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
        	
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(source);
            String fileName = "";//the name of the description file. Get from the description.
            NodeList nodeList = doc.getElementsByTagName("spec");
            Node root = nodeList.item(0);
   		 	fileName = ((Element)root).getAttribute("name");
   		 	File newFile = new File(path+fileName+".xml");
   		 	if(!newFile.exists()){
   		 		DOMSource src = new DOMSource(doc);
   		 		TransformerFactory tf = TransformerFactory.newInstance();
   		 		Transformer transformer = tf.newTransformer();
   		 		StreamResult result = new StreamResult(file);
   		 		transformer.transform(src, result); 
   		 		System.out.println("Transforming...");
   		 		
   		 		FileUtils.copyFile(file,newFile);
		 		file.delete();
	   		 	String changePath = "D:/Java/kepler/workspace/secondBlock/src/main/resources/COB-INF/demo/";
	   		 	File changeFile = new File(changePath + "deviceList.xml");
		 		System.out.println("Reading file...");
		 		
		        DocumentBuilderFactory changeFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder changeBuilder = changeFactory.newDocumentBuilder();
	            Document changeDoc = changeBuilder.parse(changeFile);
	            Element toAdd = changeDoc.createElement("Device");
	            toAdd.setAttribute("id", fileName);
	            Element var = changeDoc.createElement("Name");
	            var.setTextContent(fileName);
	            toAdd.appendChild(var);
	            changeDoc.getFirstChild().appendChild(toAdd);
	            
	            TransformerFactory tFactory = TransformerFactory.newInstance();
	            Transformer transform = tFactory.newTransformer();    
	            //transform.setOutputProperty(OutputKeys.INDENT,"yes");
	
	            DOMSource domSource = new DOMSource(changeDoc);
	            StreamResult domResult = new StreamResult(new FileOutputStream(changeFile));
	            transform.transform(domSource, domResult);
        	}
	       
        } catch (IOException e) {
            getLogger().error("StreamGenerator.generate()", e);
            throw new ResourceNotFoundException("StreamGenerator could not find resource", e);
        } catch (SAXException e) {
            getLogger().error("StreamGenerator.generate()", e);
            throw(e);
        } catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e1) {
			e1.printStackTrace();
		} finally {
            this.manager.release(parser);
        }
    }

    /**
     * Content type HTTP header can contain character encoding information,
     * for example: <code>Content-Type: text/xml; charset=UTF-8</code>.
     *
     * <p>If the servlet is following spec 2.3 and higher, the servlet API can
     * be used to retrieve character encoding part of Content-Type header. Some
     * containers can choose to not unpack charset info - the spec is not strong
     * about it. In any case, this method can be used as a last resort to
     * retrieve the passed charset value.
     *
     * <p>It is very common mistake to send : <code>Content-Type: text/xml; charset="UTF-8"</code>.
     * Some containers are not filtering this mistake and the processing results in exception.
     * This method compensates for the above mistake.
     *
     * <p>If contentType is null or has no charset part, <code>null</code> is returned.
     *
     * @param contentType value associated with Content-Type HTTP header.
     */
    public String getCharacterEncoding(Request req, String contentType) {
        if (contentType == null) {
            return null;
        }

        int idx = contentType.indexOf("charset=");
        if (idx == -1) {
            return null;
        }

        String encoding;
        try {
            encoding = req.getCharacterEncoding();
            if (encoding != null) {
                encoding = cleanup(encoding);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using charset from container: " + encoding);
                }

                return encoding;
            }
        } catch (Throwable e) {
            // We will be there if the container did not implement getCharacterEncoding() method
        }

        encoding = contentType.substring(idx + "charset=".length());
        int idxEnd = encoding.indexOf(";");
        if (idxEnd != -1) {
            encoding = encoding.substring(0, idxEnd);
        }

        encoding = cleanup(encoding);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using charset from header: " + encoding);
        }

        return encoding;
    }

    private String cleanup(String encoding) {
        encoding = encoding.trim();
        if (encoding.length() > 2 && encoding.startsWith("\"") && encoding.endsWith("\"")) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }

        return encoding;
    }
}