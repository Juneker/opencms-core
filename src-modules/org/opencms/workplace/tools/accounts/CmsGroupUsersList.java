/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupUsersList.java,v $
 * Date   : $Date: 2007/02/04 21:03:14 $
 * Version: $Revision: 1.8.4.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User groups view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.8.4.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsGroupUsersList extends A_CmsGroupUsersList {

    /** list action id constant. */
    public static final String LIST_ACTION_REMOVE = "ar";

    /** list action id constant. */
    public static final String LIST_DEFACTION_REMOVE = "dr";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_ORGUNIT = "dou";

    /** list id constant. */
    public static final String LIST_ID = "lgu";

    /** list action id constant. */
    public static final String LIST_MACTION_REMOVE = "mr";

    /** a set of action id's to use for removing. */
    protected static Set m_removeActionIds = new HashSet();

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsGroupUsersList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupUsersList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     * @param jsp an initialized JSP action element
     * @param listId the id of the specialized list
     */
    protected CmsGroupUsersList(CmsJspActionElement jsp, String listId) {

        super(jsp, listId, Messages.get().container(Messages.GUI_GROUPUSERS_LIST_NAME_0), true);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_REMOVE)) {
            // execute the remove multiaction
            Iterator itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = (CmsListItem)itItems.next();
                String userName = (String)listItem.get(LIST_COLUMN_LOGIN);
                try {
                    getCms().removeUserFromGroup(userName, getParamGroupname());
                } catch (CmsException e) {
                    // noop
                }
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        if (m_removeActionIds.contains(getParamListAction())) {
            CmsListItem listItem = getSelectedItem();
            try {
                getCms().removeUserFromGroup((String)listItem.get(LIST_COLUMN_LOGIN), getParamGroupname());
            } catch (CmsException e) {
                // should never happen
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_REMOVE_SELECTED_GROUP_0), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#getUsers(boolean)
     */
    protected List getUsers(boolean withOtherOus) throws CmsException {

        return getCms().getUsersOfGroup(getParamGroupname(), withOtherOus);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setDefaultAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setDefaultAction(CmsListColumnDefinition loginCol) {

        // add default remove action
        CmsListDefaultAction removeAction = new CmsListDefaultAction(LIST_DEFACTION_REMOVE);
        removeAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        removeAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        loginCol.addDefaultAction(removeAction);
        // keep the id
        m_removeActionIds.add(removeAction.getId());
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setIconAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    protected void setIconAction(CmsListColumnDefinition iconCol) {

        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_INGROUP_HELP_0));
        iconAction.setIconPath(A_CmsUsersList.PATH_BUTTONS + "user.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add remove multi action
        CmsListMultiAction removeMultiAction = new CmsListMultiAction(LIST_MACTION_REMOVE);
        removeMultiAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_NAME_0));
        removeMultiAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_HELP_0));
        removeMultiAction.setConfirmationMessage(Messages.get().container(Messages.GUI_USERS_LIST_MACTION_REMOVE_CONF_0));
        removeMultiAction.setIconPath(ICON_MULTI_MINUS);
        metadata.addMultiAction(removeMultiAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsGroupUsersList#setStateActionCol(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setStateActionCol(CmsListMetadata metadata) {

        // create column for state change
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_STATE);
        stateCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_0));
        stateCol.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_COLS_STATE_HELP_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add remove action
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_REMOVE);
        stateAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_NAME_0));
        stateAction.setHelpText(Messages.get().container(Messages.GUI_USERS_LIST_DEFACTION_REMOVE_HELP_0));
        stateAction.setIconPath(ICON_MINUS);
        stateCol.addDirectAction(stateAction);
        // add it to the list definition
        metadata.addColumn(stateCol);
        // keep the id
        m_removeActionIds.add(stateAction.getId());
    }
}
