/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.test.svg;

import java.io.File;

import org.apache.batik.dom.svg.SVGOMDocument;

import org.apache.batik.test.AbstractTest;

import org.w3c.dom.Document;

/**
 * Base class for tests which take an additional parameter in addition
 * to the SVG file.
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id$
 */
public class SVGReferenceRenderingAccuracyTest
    extends ParametrizedRenderingAccuracyTest {
    protected String alias;
   
    /**
     * For this type of test, the id should be made as 
     * follows:<br />
     * <fileName>#reference-alias <br />
     * For example: <br />
     * samples/anne.svg#svgView(viewBox(0,0,100,100))-viewBox1
     */
    public void setId(String id){
        this.id = id;

        String svgFile = id;

        int n = svgFile.lastIndexOf('#');
        if(n == -1 || n+1 >= svgFile.length() ){
            throw new IllegalArgumentException(id);
        }
        
        parameter = svgFile.substring(n+1, svgFile.length());
        svgFile = svgFile.substring(0, n);
        
        n = parameter.lastIndexOf('-');
        if(n == -1 || n+1 >= parameter.length()){
            throw new IllegalArgumentException(id);
        }

        alias = parameter.substring(n+1, parameter.length());
        parameter = parameter.substring(0, n);

        String[] dirNfile = breakSVGFile(svgFile);

        setConfig(buildSVGURL(dirNfile[0], dirNfile[1]),
                  buildRefImgURL(dirNfile[0], dirNfile[1]));

        setVariationURL(buildVariationURL(dirNfile[0], dirNfile[1]));
        setSaveVariation(new File(buildSaveVariationFile(dirNfile[0], dirNfile[1])));
    }

    /**
     * Gives a chance to the subclass to prepend a prefix to the 
     * svgFile name.
     * The svgURL is built as:
     * getSVGURLPrefix() + svgDir + svgFile + SVG_EXTENSION + "#" + parameter
     */
    protected String buildSVGURL(String svgDir, String svgFile){
        return getSVGURLPrefix() + svgDir +
            svgFile + SVG_EXTENSION + "#" + parameter;
    }

    /**
     * Gives a chance to the subclass to control the construction
     * of the reference PNG file from the svgFile name
     * The refImgURL is built as:
     * getRefImagePrefix() + svgDir + getRefImageSuffix() + svgFile
     */
    protected String buildRefImgURL(String svgDir, String svgFile){
        return getRefImagePrefix() + svgDir + getRefImageSuffix() + svgFile + alias + PNG_EXTENSION;
    }

    /**
     * Gives a chance to the subclass to control the construction
     * of the variation URL, which is built as:
     * getVariationPrefix() + svgDir + getVariationSuffix() + svgFile + parameter + PNG_EXTENSION
     */
    public String buildVariationURL(String svgDir, String svgFile){
        return getVariationPrefix() + svgDir + getVariationSuffix() + svgFile + alias + PNG_EXTENSION;
    }

    /**
     * Gives a chance to the subclass to control the construction
     * of the saveVariation URL, which is built as:
     * getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() + svgFile + parameter + PNG_EXTENSION
     */
    public String  buildSaveVariationFile(String svgDir, String svgFile){
        return getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() + svgFile + alias + PNG_EXTENSION;
    }

}