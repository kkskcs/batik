/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.dom.svg;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.stylesheets.DocumentStyle;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGAnimatedRect;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStringList;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGViewSpec;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

/**
 * This class implements {@link org.w3c.dom.svg.SVGSVGElement}.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class SVGOMSVGElement
    extends    SVGStylableElement
    implements SVGSVGElement {

    /**
     * The attribute initializer.
     */
    protected final static AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(6);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null,
                                          "xmlns",
                                          SVG_NAMESPACE_URI);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE,
                                          "xMidYMid meet");
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_ZOOM_AND_PAN_ATTRIBUTE,
                                          SVG_MAGNIFY_VALUE);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_VERSION_ATTRIBUTE,
                                          SVG_VERSION);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE,
                                          "text/ecmascript");
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_CONTENT_STYLE_TYPE_ATTRIBUTE,
                                          "text/css");
    }

    /**
     * Creates a new SVGOMSVGElement object.
     */
    protected SVGOMSVGElement() {
    }

    /**
     * Creates a new SVGOMSVGElement object.
     * @param prefix The namespace prefix.
     * @param owner The owner document.
     */
    public SVGOMSVGElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }

    /**
     * <b>DOM</b>: Implements {@link Node#getLocalName()}.
     */
    public String getLocalName() {
        return SVG_SVG_TAG;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getX()}.
     */
    public SVGAnimatedLength getX() {
        return getAnimatedLengthAttribute
            (null, SVG_X_ATTRIBUTE, SVG_RECT_X_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getY()}.
     */
    public SVGAnimatedLength getY() {
        return getAnimatedLengthAttribute
            (null, SVG_Y_ATTRIBUTE, SVG_SVG_Y_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getWidth()}.
     */
    public SVGAnimatedLength getWidth() {
        return getAnimatedLengthAttribute
            (null, SVG_WIDTH_ATTRIBUTE, SVG_SVG_WIDTH_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getHeight()}.
     */
    public SVGAnimatedLength getHeight() {
        return getAnimatedLengthAttribute
            (null, SVG_HEIGHT_ATTRIBUTE, SVG_SVG_HEIGHT_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getContentScriptType()}.
     */
    public String getContentScriptType() {
        return getAttributeNS(null, SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#setContentScriptType(String)}.
     */
    public void setContentScriptType(String type) {
        setAttributeNS(null, SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE, type);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getContentStyleType()}.
     */
    public String getContentStyleType() {
        return getAttributeNS(null, SVG_CONTENT_STYLE_TYPE_ATTRIBUTE);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#setContentStyleType(String)}.
     */
    public void setContentStyleType(String type) {
        setAttributeNS(null, SVG_CONTENT_STYLE_TYPE_ATTRIBUTE, type);
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#getViewport()}.
     */
    public SVGRect getViewport() {
        throw new RuntimeException(" !!! TODO: getViewport()");
    }

    public float getPixelUnitToMillimeterX( ) {
        throw new Error();
    }
    public float getPixelUnitToMillimeterY( ) {
        throw new Error();
    }
    public float getScreenPixelToMillimeterX( ) {
        throw new Error();
    }
    public float getScreenPixelToMillimeterY( ) {
        throw new Error();
    }
    public boolean getUseCurrentView( ) {
        throw new Error();
    }
    public void      setUseCurrentView( boolean useCurrentView )
        throws DOMException {
        throw new Error();
    }
    public SVGViewSpec getCurrentView( ) {
        throw new Error();
    }
    public float getCurrentScale( ) {
        throw new Error();
    }
    public void      setCurrentScale( float currentScale )
        throws DOMException {
        throw new Error();
    }
    public SVGPoint getCurrentTranslate( ) {
        throw new Error();
    }
    public int          suspendRedraw ( int max_wait_milliseconds ) {
        throw new Error();
    }
    public void          unsuspendRedraw ( int suspend_handle_id )
        throws DOMException {
        throw new Error();
    }
    public void          unsuspendRedrawAll (  ) {
        throw new Error();
    }
    public void          forceRedraw (  ) {
        throw new Error();
    }
    public void          pauseAnimations (  ) {
        throw new Error();
    }
    public void          unpauseAnimations (  ) {
        throw new Error();
    }
    public boolean       animationsPaused (  ) {
        throw new Error();
    }
    public float         getCurrentTime (  ) {
        throw new Error();
    }
    public void          setCurrentTime ( float seconds ) {
        throw new Error();
    }
    public NodeList      getIntersectionList ( SVGRect rect,
                                               SVGElement referenceElement ) {
        throw new Error();
    }
    public NodeList      getEnclosureList ( SVGRect rect,
                                            SVGElement referenceElement ) {
        throw new Error();
    }
    public boolean       checkIntersection ( SVGElement element,
                                             SVGRect rect ) {
        throw new Error();
    }
    public boolean       checkEnclosure ( SVGElement element, SVGRect rect ) {
        throw new Error();
    }
    public void          deselectAll (  ) {
        throw new Error();
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#createSVGNumber()}.
     */
    public SVGNumber createSVGNumber() {
        return new SVGNumber() {
                float value;
                public float getValue() {
                    return value;
                }
                public void setValue(float f) {
                    value = f;
                }
            };
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#createSVGLength()}.
     */
    public SVGLength createSVGLength() {
        throw new RuntimeException("!!! TODO: createSVGNumber()");
    }

    public SVGAngle               createSVGAngle (  ) {
        throw new Error();
    }

    /**
     * <b>DOM</b>: Implements {@link SVGSVGElement#createSVGPoint()}.
     */
    public SVGPoint createSVGPoint() {
        return new SVGPoint() {
                float x;
                float y;
                public float getX() {
                    return x;
                }
                public void setX(float x) throws DOMException {
                    this.x = x;
                }
                public float getY() {
                    return y;
                }
                public void setY(float y) throws DOMException {
                    this.y = y;
                }
                public SVGPoint matrixTransform(SVGMatrix matrix) {
                    throw new RuntimeException("!!! TODO: matrixTransform()");
                }
            };
    }

    public SVGMatrix              createSVGMatrix (  ) {
        throw new Error();
    }
    public SVGRect                createSVGRect (  ) {
        throw new Error();
    }
    public SVGTransform           createSVGTransform (  ) {
        throw new Error();
    }
    public SVGTransform     createSVGTransformFromMatrix ( SVGMatrix matrix ) {
        throw new Error();
    }
    public String              createSVGString (  ) {
        throw new Error();
    }
    public Element         getElementById ( String elementId ) {
        throw new Error();
    }

    // SVGLocatable ///////////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getNearestViewportElement()}.
     */
    public SVGElement getNearestViewportElement() {
	return SVGLocatableSupport.getNearestViewportElement(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getFarthestViewportElement()}.
     */
    public SVGElement getFarthestViewportElement() {
	return SVGLocatableSupport.getFarthestViewportElement(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getBBox()}.
     */
    public SVGRect getBBox() {
	return SVGLocatableSupport.getBBox(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getCTM()}.
     */
    public SVGMatrix getCTM() {
	return SVGLocatableSupport.getCTM(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getScreenCTM()}.
     */
    public SVGMatrix getScreenCTM() {
	return SVGLocatableSupport.getScreenCTM(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGLocatable#getTransformToElement(SVGElement)}.
     */
    public SVGMatrix getTransformToElement(SVGElement element)
	throws SVGException {
	return SVGLocatableSupport.getTransformToElement(this, element);
    }

    // ViewCSS ////////////////////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.views.AbstractView#getDocument()}.
     */
    public DocumentView getDocument() {
        return (DocumentView)getOwnerDocument();
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.css.ViewCSS#getComputedStyle(Element,String)}.
     */
    public CSSStyleDeclaration getComputedStyle(Element elt,
                                                String pseudoElt) {
        AbstractView av = ((DocumentView)getOwnerDocument()).getDefaultView();
        return ((ViewCSS)av).getComputedStyle(elt, pseudoElt);
    }

    // DocumentEvent /////////////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.events.DocumentEvent#createEvent(String)}.
     */
    public Event createEvent(String eventType) throws DOMException {
        return ((DocumentEvent)getOwnerDocument()).createEvent(eventType);
    }

    // DocumentCSS ////////////////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.stylesheets.DocumentStyle#getStyleSheets()}.
     */
    public StyleSheetList getStyleSheets() {
        return ((DocumentStyle)getOwnerDocument()).getStyleSheets();
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.css.DocumentCSS#getOverrideStyle(Element,String)}.
     */
    public CSSStyleDeclaration getOverrideStyle(Element elt,
                                                String pseudoElt) {
        return ((DocumentCSS)getOwnerDocument()).getOverrideStyle(elt,
                                                                  pseudoElt);
    }

    // SVGLangSpace support //////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Returns the xml:lang attribute value.
     */
    public String getXMLlang() {
        return XMLSupport.getXMLLang(this);
    }

    /**
     * <b>DOM</b>: Sets the xml:lang attribute value.
     */
    public void setXMLlang(String lang) {
        setAttributeNS(XMLSupport.XML_NAMESPACE_URI,
                       XMLSupport.XML_LANG_ATTRIBUTE,
                       lang);
    }

    /**
     * <b>DOM</b>: Returns the xml:space attribute value.
     */
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }

    /**
     * <b>DOM</b>: Sets the xml:space attribute value.
     */
    public void setXMLspace(String space) {
        setAttributeNS(XMLSupport.XML_NAMESPACE_URI,
                       XMLSupport.XML_SPACE_ATTRIBUTE,
                       space);
    }

    // SVGZoomAndPan support ///////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGZoomAndPan#getZoomAndPan()}.
     */
    public short getZoomAndPan() {
        return SVGZoomAndPanSupport.getZoomAndPan(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGZoomAndPan#getZoomAndPan()}.
     */
    public void setZoomAndPan(short val) {
        SVGZoomAndPanSupport.setZoomAndPan(this, val);
    }

    // SVGFitToViewBox support ////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGFitToViewBox#getViewBox()}.
     */
    public SVGAnimatedRect getViewBox() {
        throw new RuntimeException(" !!! TODO: getViewBox()");
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGFitToViewBox#getPreserveAspectRatio()}.
     */
    public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
        throw new RuntimeException(" !!! TODO: getPreserveAspectRatio()");
    }

    // SVGExternalResourcesRequired support /////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGExternalResourcesRequired#getExternalResourcesRequired()}.
     */
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return SVGExternalResourcesRequiredSupport.
            getExternalResourcesRequired(this);
    }

    // SVGTests support ///////////////////////////////////////////////////

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGTests#getRequiredFeatures()}.
     */
    public SVGStringList getRequiredFeatures() {
        return SVGTestsSupport.getRequiredFeatures(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGTests#getRequiredExtensions()}.
     */
    public SVGStringList getRequiredExtensions() {
        return SVGTestsSupport.getRequiredExtensions(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGTests#getSystemLanguage()}.
     */
    public SVGStringList getSystemLanguage() {
        return SVGTestsSupport.getSystemLanguage(this);
    }

    /**
     * <b>DOM</b>: Implements {@link
     * org.w3c.dom.svg.SVGTests#hasExtension(String)}.
     */
    public boolean hasExtension(String extension) {
        return SVGTestsSupport.hasExtension(this, extension);
    }

    /**
     * Returns the AttributeInitializer for this element type.
     * @return null if this element has no attribute with a default value.
     */
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }

    /**
     * Returns a new uninitialized instance of this object's class.
     */
    protected Node newNode() {
        return new SVGOMSVGElement();
    }
}
