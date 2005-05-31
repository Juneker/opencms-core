/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsPointerLoader.java,v $
 * Date   : $Date: 2005/05/31 14:39:21 $
 * Version: $Revision: 1.40 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Loader for "pointers" to resources in the VFS or to external resources.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.40 $
 */
public class CmsPointerLoader implements I_CmsResourceLoader {

    /** The id of this loader. */
    public static final int C_RESOURCE_LOADER_ID = 4;

    /** The html-code prefix for generating the export file for external links. */
    private static String C_EXPORT_PREFIX = "<html>\n<head>\n<meta http-equiv="
        + '"'
        + "refresh"
        + '"'
        + " content="
        + '"'
        + "0; url=";
    
    /** The html-code suffix for generating the export file for external links. */
    private static String C_EXPORT_SUFFIX = '"' + ">\n</head>\n<body></body>\n</html>";

    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public CmsPointerLoader() {

        // NOOP
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // this resource loader requires no parameters     
    }

    /** 
     * Destroy this ResourceLoder, this is a NOOP so far.<p>
     */
    public void destroy() {

        // NOOP
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String, java.util.Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] dump(
        CmsObject cms,
        CmsResource resource,
        String element,
        Locale locale,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException {

        return CmsFile.upgrade(resource, cms).getContents();
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public byte[] export(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        String pointer = new String(CmsFile.upgrade(resource, cms).getContents());
        StringBuffer result = new StringBuffer(128);
        result.append(C_EXPORT_PREFIX);
        result.append(pointer);
        result.append(C_EXPORT_SUFFIX);
        load(cms, resource, req, res);
        return result.toString().getBytes(OpenCms.getSystemInfo().getDefaultEncoding());
    }

    /**
     * Will always return <code>null</code> since this loader does not 
     * need to be cnofigured.<p>
     * 
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        return null;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#getLoaderId()
     */
    public int getLoaderId() {

        return C_RESOURCE_LOADER_ID;
    }

    /**
     * Return a String describing the ResourceLoader,
     * which is <code>"The OpenCms default resource loader for pointers"</code>.<p>
     * 
     * @return a describing String for the ResourceLoader 
     */
    public String getResourceLoaderInfo() {

        return "The OpenCms default resource loader for pointers";
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(
                Messages.INIT_LOADER_INITIALIZED_1, this.getClass().getName()));            
        }
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled()
     */
    public boolean isStaticExportEnabled() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable()
     */
    public boolean isStaticExportProcessable() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates()
     */
    public boolean isUsableForTemplates() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate()
     */
    public boolean isUsingUriWhenLoadingTemplate() {

        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void load(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException {

        if (res == null || res.isCommitted()) {
            // nothing we can do
            return;
        }    
        
        String pointer = new String(CmsFile.upgrade(resource, cms).getContents());
        if (pointer == null || "".equals(pointer.trim())) {
            throw new CmsLoaderException(Messages.get().container(Messages.ERR_INVALID_POINTER_FILE_1, resource.getName()));
        }        
        res.sendRedirect(pointer);
    }

    /**
     * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject, org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(CmsObject cms, CmsResource file, ServletRequest req, ServletResponse res) {

        throw new CmsRuntimeException(Messages.get().container(
            Messages.ERR_SERVICE_UNSUPPORTED_1,
            this.getClass().getName()));
    }
}