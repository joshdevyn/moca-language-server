package com.github.joshdevyn.mocaide.gui.editor;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

/**
 * Custom token maker factory for MOCA language support
 */
public class MocaTokenMakerFactory extends AbstractTokenMakerFactory {
    
    @Override
    protected void initTokenMakerMap() {
        // Register MOCA as a custom language
        putMapping("text/moca", "com.github.joshdevyn.mocaide.gui.editor.MocaTokenMaker");
    }
    
    /**
     * Install the MOCA token maker factory
     */
    public static void install() {
        TokenMakerFactory.setDefaultInstance(new MocaTokenMakerFactory());
    }
}
