package org.mypomodoro.util;

import java.awt.Cursor;
import org.mypomodoro.Main;

/**
 * Wait cursor
 *
 */
public class WaitCursor {

    private static boolean started = false;

    /**
     * Start wait cursor
     *
     */
    public static void startWaitCursor() {
        Main.gui.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Main.gui.getGlassPane().setVisible(true);
        started = true;
    }

    /**
     * Stop wait cursor
     *
     */
    public static void stopWaitCursor() {
        Main.gui.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Main.gui.getGlassPane().setVisible(false);
        started = false;
    }

    public static boolean isStarted() {
        return started;
    }
}
