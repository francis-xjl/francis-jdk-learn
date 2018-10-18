/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.lwawt.macosx;

import com.apple.eawt.Application;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Taskbar.Feature;
import java.awt.peer.TaskbarPeer;

final public class CTaskbarPeer implements TaskbarPeer {

    CTaskbarPeer() {}

    @Override
    public boolean isSupported(Feature feature) {
        switch(feature) {
            case ICON_BADGE_TEXT:
            case ICON_BADGE_NUMBER:
            case ICON_IMAGE:
            case MENU:
            case PROGRESS_VALUE:
            case USER_ATTENTION:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setProgressValue(int value) {
        Application.getApplication().setDockIconProgress(value);
    }

    @Override
    public void setIconBadge(String badge) {
        Application.getApplication().setDockIconBadge(badge);
    }

    @Override
    public Image getIconImage() {
        return Application.getApplication().getDockIconImage();
    }

    @Override
    public void setIconImage(Image image) {
        Application.getApplication().setDockIconImage(image);
    }

    @Override
    public PopupMenu getMenu() {
        return Application.getApplication().getDockMenu();
    }

    @Override
    public void setMenu(PopupMenu menu) {
        Application.getApplication().setDockMenu(menu);
    }

    @Override
    public void requestUserAttention(boolean enabled, boolean critical) {
        if (enabled) {
            Application.getApplication().requestUserAttention(critical);
        }
    }
}
