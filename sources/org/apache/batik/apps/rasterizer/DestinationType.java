/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.apps.rasterizer;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;

/**
 * Describes the type of destination for an <tt>SVGConverter</tt>
 * operation.
 *
 * @author Henri Ruini
 * @author <a href="mailto:vhardy@apache.org">Vincent Hardy</a>
 * @version $Id$
 */
public final class DestinationType {
    public static final String PNG_STR = "image/png";
    public static final String JPEG_STR = "image/jpeg";
    public static final String TIFF_STR = "image/tiff";
    public static final String PDF_STR = "application/pdf";

    public static final int PNG_CODE = 0;
    public static final int JPEG_CODE = 1;
    public static final int TIFF_CODE = 2;
    public static final int PDF_CODE = 3;
        
    public static final String PNG_EXTENSION = ".png";
    public static final String JPEG_EXTENSION = ".jpg";
    public static final String TIFF_EXTENSION = ".tiff";
    public static final String PDF_EXTENSION = ".pdf";

    public static final DestinationType PNG  = new DestinationType(PNG_STR, PNG_CODE, PNG_EXTENSION);
    public static final DestinationType JPEG = new DestinationType(JPEG_STR, JPEG_CODE, JPEG_EXTENSION);
    public static final DestinationType TIFF = new DestinationType(TIFF_STR, TIFF_CODE, TIFF_EXTENSION);
    public static final DestinationType PDF  = new DestinationType(PDF_STR, PDF_CODE, PDF_EXTENSION);

    private String type;
    private int code;
    private String extension;

    private DestinationType(String type, int code, String extension){
        this.type = type;
        this.code = code;
        this.extension = extension;
    }
    
    public String getExtension(){
        return extension;
    }

    public String toString(){
        return type;
    }

    public int toInt(){
        return code;
    }

    /**
     * Returns a transcoder object of the result image type.
     *
     * @param type Type of the result image.
     *
     * @return Transcoder object or <tt>null</tt> if there isn't a proper transcoder.
     */
    protected Transcoder getTranscoder(){
        switch(code) {
            case PNG_CODE:
                return new PNGTranscoder();
            case JPEG_CODE:
                return new JPEGTranscoder();
            case TIFF_CODE:
                return new TIFFTranscoder();
            case PDF_CODE:
                try {
                    Class pdfClass = Class.forName("org.apache.fop.svg.PDFTranscoder");
                    return (Transcoder)pdfClass.newInstance();
                } catch(Exception e) {
                    return null;
                }
            default:
                return null;
        }

    }

    /**
     * Defines valid image types.
     *
     * @return Array of valid values as strings.
     */
    public DestinationType[] getValues() {
        return new DestinationType[]{PNG, JPEG, TIFF, PDF};
    }

    public Object readResolve(){
        switch(code){
        case PNG_CODE:
            return PNG;
        case JPEG_CODE:
            return JPEG;
        case TIFF_CODE:
            return TIFF;
        case PDF_CODE:
            return PDF;
        default:
            throw new Error();
        }
    }
}