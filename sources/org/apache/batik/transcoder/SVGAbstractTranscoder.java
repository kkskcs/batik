/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.transcoder;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import java.net.URL;

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.ExtensibleSVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.DocumentFactory;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;

import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.BridgeExtension;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;

import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.Rectangle2DKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.batik.transcoder.image.resources.Messages;

import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.ParsedURL;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGSVGElement;


/**
 * This class may be the base class of all transcoders which take an
 * SVG document as input and which need to build a DOM tree. The
 * <tt>SVGAbstractTranscoder</tt> uses several different hints that
 * guide it's behaviour:<br/>
 *
 * <ul>
 *   <li><tt>KEY_WIDTH, KEY_HEIGHT</tt> can be used to specify how to scale the
 *       SVG image</li>
 * </ul>
 *
 * @author <a href="mailto:Thierry.Kormann@sophia.inria.fr">Thierry Kormann</a>
 * @version $Id$ */
public abstract class SVGAbstractTranscoder extends XMLAbstractTranscoder {
    /**
     * Current area of interest.
     */
    protected Rectangle2D curAOI;

    /**
     * Transform needed to render the current area of interest
     */
    protected AffineTransform curTxf;

    /**
     * Current GVT Tree, i.e., the GVT tree representing the page
     * being printed currently.
     */
    protected GraphicsNode root;

    protected float width, height;

    /** The user agent dedicated to an SVG Transcoder. */
    protected UserAgent userAgent;

    protected SVGAbstractTranscoder() {
        userAgent = createUserAgent();

        hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                  SVGConstants.SVG_NAMESPACE_URI);
        hints.put(KEY_DOCUMENT_ELEMENT,
                  SVGConstants.SVG_SVG_TAG);
        hints.put(KEY_DOM_IMPLEMENTATION,
                  ExtensibleSVGDOMImplementation.getDOMImplementation());
        hints.put(KEY_MEDIA,
                  "screen");
        hints.put(KEY_EXECUTE_ONLOAD, 
                  Boolean.FALSE);
        hints.put(KEY_ALLOWED_SCRIPT_TYPES,
                  DEFAULT_ALLOWED_SCRIPT_TYPES);
    }


    protected UserAgent createUserAgent() {
        return new SVGAbstractTranscoderUserAgent();
    }

    /**
     * Creates a <tt>DocumentFactory</tt> that is used to create an SVG DOM
     * tree. The specified DOM Implementation is ignored and the Batik
     * SVG DOM Implementation is automatically used.
     *
     * @param domImpl the DOM Implementation (not used)
     * @param parserClassname the XML parser classname
     */
    protected DocumentFactory createDocumentFactory(DOMImplementation domImpl,
                                                    String parserClassname) {
        return new SAXSVGDocumentFactory(parserClassname);
    }

    /**
     * Transcodes the specified Document as an image in the specified output.
     *
     * @param document the document to transcode
     * @param uri the uri of the document or null if any
     * @param output the ouput where to transcode
     * @exception TranscoderException if an error occured while transcoding
     */
    protected void transcode(Document document,
                             String uri,
                             TranscoderOutput output)
            throws TranscoderException {

        if (!(document instanceof SVGOMDocument)) {
            throw new TranscoderException(
                Messages.formatMessage("notsvg", null));
        }

        BridgeContext ctx = new BridgeContext(userAgent);
        SVGOMDocument svgDoc = (SVGOMDocument)document;
        SVGSVGElement root = svgDoc.getRootElement();

        // build the GVT tree
        GVTBuilder builder = new GVTBuilder();
        // flag that indicates if the document is dynamic
        boolean isDynamic = 
            (hints.containsKey(KEY_EXECUTE_ONLOAD) &&
             ((Boolean)hints.get(KEY_EXECUTE_ONLOAD)).booleanValue() &&
             BaseScriptingEnvironment.isDynamicDocument(svgDoc));
        ctx.setDynamic(isDynamic);

        GraphicsNode gvtRoot;
        try {
            gvtRoot = builder.build(ctx, svgDoc);
            // dispatch an 'onload' event if needed
            if (ctx.isDynamic()) {
                BaseScriptingEnvironment se = new BaseScriptingEnvironment(ctx);
                se.loadScripts();
                se.dispatchSVGLoadEvent();
            }
        } catch (BridgeException ex) {
            throw new TranscoderException(ex);
        }
        // get the 'width' and 'height' attributes of the SVG document
        float docWidth = (float)ctx.getDocumentSize().getWidth();
        float docHeight = (float)ctx.getDocumentSize().getHeight();
        ctx = null;
        builder = null;

        // compute the image's width and height according the hints
        float imgWidth = -1;
        if (hints.containsKey(KEY_WIDTH)) {
            imgWidth = ((Float)hints.get(KEY_WIDTH)).floatValue();
        }
        float imgHeight = -1;
        if (hints.containsKey(KEY_HEIGHT)) {
            imgHeight = ((Float)hints.get(KEY_HEIGHT)).floatValue();
        }

        if (imgWidth > 0 && imgHeight > 0) {
            width = imgWidth;
            height = imgHeight;
        } else if (imgHeight > 0) {
            width = (docWidth * imgHeight) / docHeight;
            height = imgHeight;
        } else if (imgWidth > 0) {
            width = imgWidth;
            height = (docHeight * imgWidth) / docWidth;
        } else {
            width = docWidth;
            height = docHeight;
        }
        // compute the preserveAspectRatio matrix
        AffineTransform Px;
        String ref = new ParsedURL(uri).getRef();

        try {
            Px = ViewBox.getViewTransform(ref, root, width, height);
        } catch (BridgeException ex) {
            throw new TranscoderException(ex);
        }

        if (Px.isIdentity() && (width != docWidth || height != docHeight)) {
            // The document has no viewBox, we need to resize it by hand.
            // we want to keep the document size ratio
            float d = Math.max(docWidth, docHeight);
            float dd = Math.max(width, height);
            float scale = dd/d;
            Px = AffineTransform.getScaleInstance(scale, scale);
        }
        // take the AOI into account if any
        if (hints.containsKey(KEY_AOI)) {
            Rectangle2D aoi = (Rectangle2D)hints.get(KEY_AOI);
            // transform the AOI into the image's coordinate system
            aoi = Px.createTransformedShape(aoi).getBounds2D();
            AffineTransform Mx = new AffineTransform();
            double sx = width / aoi.getWidth();
            double sy = height / aoi.getHeight();
            Mx.scale(sx, sy);
            double tx = -aoi.getX();
            double ty = -aoi.getY();
            Mx.translate(tx, ty);
            // take the AOI transformation matrix into account
            // we apply first the preserveAspectRatio matrix
            Px.preConcatenate(Mx);
            curAOI = aoi;
        } else {
            curAOI = new Rectangle2D.Float(0, 0, width, height);
        }
        curTxf = Px;
        this.root = gvtRoot;
    }

    // --------------------------------------------------------------------
    // Keys definition
    // --------------------------------------------------------------------

    /**
     * The image width key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_WIDTH</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Float</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">The width of the top most svg element</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the width of the image to create.</TD></TR>
     * </TABLE> */
    public static final TranscodingHints.Key KEY_WIDTH
        = new LengthKey();

    /**
     * The image height key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_HEIGHT</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Float</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">The height of the top most svg element</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the height of the image to create.</TD></TR>
     * </TABLE> */
    public static final TranscodingHints.Key KEY_HEIGHT
        = new LengthKey();


    /**
     * The area of interest key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_AOI</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Rectangle2D</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">The document's size</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the area of interest to render. The
     * rectangle coordinates must be specified in pixels and in the
     * document coordinates system.</TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_AOI
        = new Rectangle2DKey();

    /**
     * The language key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_LANGUAGE</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">String</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">"en"</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the preferred language of the document.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_LANGUAGE
        = new StringKey();

    /**
     * The media key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_MEDIA</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">String</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">"screen"</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the media to use with CSS.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_MEDIA
        = new StringKey();

    /**
     * The alternate stylesheet key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_ALTERNATE_STYLESHEET</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">String</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">null</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the alternate style sheet title.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_ALTERNATE_STYLESHEET
        = new StringKey();

    /**
     * The user stylesheet URI key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_USER_STYLESHEET_URI</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">String</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">null</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the user style sheet.</TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_USER_STYLESHEET_URI
        = new StringKey();

    /**
     * The number of millimeters in each pixel key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_PIXEL_UNIT_TO_MILLIMETER</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Float</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">0.264583</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the size of a px CSS unit in millimeters.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_PIXEL_UNIT_TO_MILLIMETER
        = new FloatKey();

    /**
     * The pixel to millimeter conversion factor key.
     * @deprecated As of Batik Version 1.5b3
     * @see #KEY_MM_PER_PIXEL
     *
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_PIXEL_TO_MM</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Float</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">0.264583</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify the size of a px CSS unit in millimeters.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_PIXEL_TO_MM 
        = KEY_PIXEL_UNIT_TO_MILLIMETER;

    /**
     * The 'onload' execution key.
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_EXECUTE_ONLOAD</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">Boolean</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">false</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specify if scripts added on the 'onload' event 
     * attribute must be invoked.</TD></TR>
     * </TABLE> 
     */
    public static final TranscodingHints.Key KEY_EXECUTE_ONLOAD
        = new BooleanKey();

    /**
     * The set of supported script languages (i.e., the set of possible
     * values for the &lt;script&gt; tag's type attribute).
     *
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_ALLOWED_SCRIPT_TYPES</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">String (Comma separated values)</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">text/ecmascript, application/java-archive</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">Specifies the allowed values for the type attribute
     * in the &lt;script&gt; element. This is a comma separated list. The
     * special value '*' means that all script types are allowed.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_ALLOWED_SCRIPT_TYPES
        = new StringKey();

    /**
     * Default value for the KEY_ALLOWED_SCRIPT_TYPES key
     */
    public static final String DEFAULT_ALLOWED_SCRIPT_TYPES
        = SVGConstants.SVG_SCRIPT_TYPE_ECMASCRIPT + ", " 
        + SVGConstants.SVG_SCRIPT_TYPE_JAVA;

    /**
     * Controls whether or not scripts can only be loaded from the 
     * same location as the document which references them.
     *
     * <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1">
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Key: </TH>
     * <TD VALIGN="TOP">KEY_CONSTRAIN_SCRIPT_ORIGIN</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Value: </TH>
     * <TD VALIGN="TOP">boolean</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Default: </TH>
     * <TD VALIGN="TOP">true</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Required: </TH>
     * <TD VALIGN="TOP">No</TD></TR>
     * <TR>
     * <TH VALIGN="TOP" ALIGN="RIGHT"><P ALIGN="RIGHT">Description: </TH>
     * <TD VALIGN="TOP">When set to true, script elements referencing
     * files from a different origin (server) than the document containing
     * the script element will not be loaded. When set to true, script elements
     * may reference script files from any origin.
     * </TD></TR>
     * </TABLE>
     */
    public static final TranscodingHints.Key KEY_CONSTRAIN_SCRIPT_ORIGIN
        = new BooleanKey();


    /**
     * A user agent implementation for <tt>PrintTranscoder</tt>.
     */
    protected class SVGAbstractTranscoderUserAgent extends UserAgentAdapter {
        /**
         * Vector containing the allowed script types
         */
        protected Vector scripts;

        public SVGAbstractTranscoderUserAgent() {
            addStdFeatures();
        }

        /**
         * Returns the default size of this user agent (400x400).
         */
        public Dimension2D getViewportSize() {
            return new Dimension(400, 400);
        }

        /**
         * Displays the specified error message using the <tt>ErrorHandler</tt>.
         */
        public void displayError(String message) {
            try {
                SVGAbstractTranscoder.this.handler.error
                    (new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified error using the <tt>ErrorHandler</tt>.
         */
        public void displayError(Exception e) {
            try {
                SVGAbstractTranscoder.this.handler.error
                    (new TranscoderException(e));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified message using the <tt>ErrorHandler</tt>.
         */
        public void displayMessage(String message) {
            try {
                SVGAbstractTranscoder.this.handler.warning
                    (new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Returns the pixel to millimeter conversion factor specified in the
         * <tt>TranscodingHints</tt> or 0.26458333 if not specified.
         */
        public float getPixelUnitToMillimeter() {
            Object obj = SVGAbstractTranscoder.this.hints.get
                (KEY_PIXEL_UNIT_TO_MILLIMETER);
            if (obj != null) {
                return ((Float)obj).floatValue();
            }

            return super.getPixelUnitToMillimeter();
        }

        /**
         * Returns the user language specified in the
         * <tt>TranscodingHints</tt> or "en" (english) if any.
         */
        public String getLanguages() {
            if (SVGAbstractTranscoder.this.hints.containsKey(KEY_LANGUAGE)) {
                return (String)SVGAbstractTranscoder.this.hints.get
                    (KEY_LANGUAGE);
            }
             
            return super.getLanguages();
        }

        /**
         * Returns this user agent's CSS media.
         */
        public String getMedia() {
            String s = (String)hints.get(KEY_MEDIA);
            if (s != null) return s;

            return super.getMedia();
        }

        /**
         * Returns this user agent's alternate style-sheet title.
         */
        public String getAlternateStyleSheet() {
            String s = (String)hints.get(KEY_ALTERNATE_STYLESHEET);
            if (s != null)
                return s;

            return super.getAlternateStyleSheet();
        }

        /**
         * Returns the user stylesheet specified in the
         * <tt>TranscodingHints</tt> or null if any.
         */
        public String getUserStyleSheetURI() {
            String s = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_USER_STYLESHEET_URI);
            if (s != null)
                return s;

            return super.getUserStyleSheetURI();
        }

        /**
         * Returns the XML parser to use from the TranscodingHints.
         */
        public String getXMLParserClassName() {
            String s = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_XML_PARSER_CLASSNAME);
            if (s != null)
                return s;

            return super.getXMLParserClassName();
        }

        /**
    	 * Returns true if the XML parser must be in validation mode, false
    	 * otherwise.
    	 */
        public boolean isXMLParserValidating() {
            Boolean b = (Boolean)SVGAbstractTranscoder.this.hints.get
                (KEY_XML_PARSER_VALIDATING);
            if (b != null)
                return b.booleanValue();

            return super.isXMLParserValidating();
        }

        /**
         * Returns the security settings for the given script
         * type, script url and document url
         * 
         * @param scriptType type of script, as found in the 
         *        type attribute of the &lt;script&gt; element.
         * @param scriptURL url for the script, as defined in
         *        the script's xlink:href attribute. If that
         *        attribute was empty, then this parameter should
         *        be null
         * @param docURL url for the document into which the 
         *        script was found.
         */
        public ScriptSecurity getScriptSecurity(String scriptType,
                                                ParsedURL scriptPURL,
                                                ParsedURL docPURL){
            if (scripts == null){
                computeAllowedScripts();
            }

            if (!scripts.contains(scriptType)) {
                return new NoLoadScriptSecurity(scriptType);
            }


            boolean constrainOrigin = true;

            if (SVGAbstractTranscoder.this.hints.containsKey
                (KEY_CONSTRAIN_SCRIPT_ORIGIN)) {
                constrainOrigin =
                    ((Boolean)SVGAbstractTranscoder.this.hints.get
                     (KEY_CONSTRAIN_SCRIPT_ORIGIN)).booleanValue();
            } 

            if (constrainOrigin) {
                return new DefaultScriptSecurity
                    (scriptType,scriptPURL,docPURL);
            } else {
                return new RelaxedScriptSecurity
                    (scriptType,scriptPURL,docPURL);
            }
        }

        /**
         * Helper method. Builds a Vector containing the allowed
         * values for the &lt;script&gt; element's type attribute.
         */
        protected void computeAllowedScripts(){
            scripts = new Vector();
            if (!SVGAbstractTranscoder.this.hints.containsKey
                (KEY_ALLOWED_SCRIPT_TYPES)) {
                return;
            }

            String allowedScripts 
                = (String)SVGAbstractTranscoder.this.hints.get
                (KEY_ALLOWED_SCRIPT_TYPES);
                
            StringTokenizer st = new StringTokenizer(allowedScripts, ",");
            while (st.hasMoreTokens()) {
                scripts.addElement(st.nextToken());
            }
        }

    }
}