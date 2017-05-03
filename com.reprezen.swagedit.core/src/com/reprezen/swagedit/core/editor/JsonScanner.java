/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.editor;

import java.util.ArrayList;

import org.dadacoalition.yedit.editor.ColorManager;
import org.dadacoalition.yedit.editor.scanner.AnchorWordDetector;
import org.dadacoalition.yedit.editor.scanner.DocumentStartAndEndRule;
import org.dadacoalition.yedit.editor.scanner.DoubleQuotedKeyRule;
import org.dadacoalition.yedit.editor.scanner.IndicatorCharacterRule;
import org.dadacoalition.yedit.editor.scanner.KeyRule;
import org.dadacoalition.yedit.editor.scanner.PredefinedValueRule;
import org.dadacoalition.yedit.editor.scanner.ScalarRule;
import org.dadacoalition.yedit.editor.scanner.SingleQuotedKeyRule;
import org.dadacoalition.yedit.editor.scanner.TagWordDetector;
import org.dadacoalition.yedit.editor.scanner.WhitespaceRule;
import org.dadacoalition.yedit.editor.scanner.YAMLScanner;
import org.dadacoalition.yedit.editor.scanner.YAMLToken;
import org.dadacoalition.yedit.preferences.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/*
 * Identical implementation of a BufferedRuleBasedScanner than YAMLScanner but makes 
 * use of SwagEdit PreferenceStore to set SwagEdit colors instead of YEdit colors.
 * 
 * This implementation is necessary due to the lack of possibility to override YAMLScanner
 * usage of a preference store.
 */
public class JsonScanner extends YAMLScanner implements IPartitionTokenScanner {

    private ColorManager colorManager;
    private IPreferenceStore store;

    public JsonScanner(ColorManager colorManager, IPreferenceStore store) {
        super(colorManager);

        this.colorManager = colorManager;
        this.store = store;
        init();
    }

    protected void init() {
        TextAttribute keyAttr = tokenAttribute(PreferenceConstants.COLOR_KEY, PreferenceConstants.BOLD_KEY,
                PreferenceConstants.ITALIC_KEY, PreferenceConstants.UNDERLINE_KEY);

        IToken keyToken = new YAMLToken(keyAttr, YAMLToken.KEY);

        TextAttribute scalarAttr = tokenAttribute(PreferenceConstants.COLOR_SCALAR, PreferenceConstants.BOLD_SCALAR,
                PreferenceConstants.ITALIC_SCALAR, PreferenceConstants.UNDERLINE_SCALAR);
        IToken scalarToken = new YAMLToken(scalarAttr, YAMLToken.SCALAR);

        TextAttribute commentAttr = tokenAttribute(PreferenceConstants.COLOR_COMMENT, PreferenceConstants.BOLD_COMMENT,
                PreferenceConstants.ITALIC_COMMENT, PreferenceConstants.UNDERLINE_COMMENT);
        IToken commentToken = new YAMLToken(commentAttr, YAMLToken.COMMENT);

        TextAttribute documentAttr = tokenAttribute(PreferenceConstants.COLOR_DOCUMENT,
                PreferenceConstants.BOLD_DOCUMENT, PreferenceConstants.ITALIC_DOCUMENT,
                PreferenceConstants.UNDERLINE_DOCUMENT);

        IToken documentStartToken = new YAMLToken(documentAttr, YAMLToken.DOCUMENT_START);
        IToken documentEndToken = new YAMLToken(documentAttr, YAMLToken.DOCUMENT_END);

        TextAttribute anchorAttr = tokenAttribute(PreferenceConstants.COLOR_ANCHOR, PreferenceConstants.BOLD_ANCHOR,
                PreferenceConstants.ITALIC_ANCHOR, PreferenceConstants.UNDERLINE_ANCHOR);
        IToken anchorToken = new YAMLToken(anchorAttr, YAMLToken.ANCHOR);

        TextAttribute aliasAttr = tokenAttribute(PreferenceConstants.COLOR_ALIAS, PreferenceConstants.BOLD_ALIAS,
                PreferenceConstants.ITALIC_ALIAS, PreferenceConstants.UNDERLINE_ALIAS);
        IToken aliasToken = new YAMLToken(aliasAttr, YAMLToken.ALIAS);

        IToken indicatorCharToken = new YAMLToken(new TextAttribute(null), YAMLToken.INDICATOR_CHARACTER);

        TextAttribute tagAttr = tokenAttribute(PreferenceConstants.COLOR_TAG_PROPERTY,
                PreferenceConstants.BOLD_TAG_PROPERTY, PreferenceConstants.ITALIC_TAG_PROPERTY,
                PreferenceConstants.UNDERLINE_TAG_PROPERTY);
        IToken tagPropToken = new YAMLToken(tagAttr, YAMLToken.TAG_PROPERTY);

        TextAttribute constantAttr = tokenAttribute(PreferenceConstants.COLOR_CONSTANT,
                PreferenceConstants.BOLD_CONSTANT, PreferenceConstants.ITALIC_CONSTANT,
                PreferenceConstants.UNDERLINE_CONSTANT);
        IToken predefinedValToken = new YAMLToken(constantAttr, YAMLToken.CONSTANT);

        IToken whitespaceToken = new YAMLToken(new TextAttribute(null), YAMLToken.WHITESPACE);

        IToken directiveToken = new YAMLToken(new TextAttribute(null), YAMLToken.DIRECTIVE);

        ArrayList<IRule> rules = new ArrayList<IRule>();

        rules.add(new KeyRule(keyToken));
        rules.add(new SingleQuotedKeyRule(keyToken));
        rules.add(new DoubleQuotedKeyRule(keyToken));
        rules.add(new MultiLineRule("\"", "\"", scalarToken, '\\'));
        rules.add(new MultiLineRule("'", "'", scalarToken));
        rules.add(new EndOfLineRule("#", commentToken));
        rules.add(new EndOfLineRule("%TAG", directiveToken));
        rules.add(new EndOfLineRule("%YAML", directiveToken));
        rules.add(new DocumentStartAndEndRule("---", documentStartToken));
        rules.add(new DocumentStartAndEndRule("...", documentEndToken));
        rules.add(new IndicatorCharacterRule(indicatorCharToken));
        rules.add(new WhitespaceRule(whitespaceToken));
        rules.add(new WordPatternRule(new AnchorWordDetector(), "&", "", anchorToken));
        rules.add(new WordPatternRule(new AnchorWordDetector(), "*", "", aliasToken));
        rules.add(new WordPatternRule(new TagWordDetector(), "!", "", tagPropToken));

        rules.add(new PredefinedValueRule(predefinedValToken));

        rules.add(new ScalarRule(scalarToken));

        IRule[] rulesArray = new IRule[rules.size()];
        rules.toArray(rulesArray);
        setRules(rulesArray);
        setDefaultReturnToken(scalarToken);

    }

    private TextAttribute tokenAttribute(String colorPrefs, String boldPrefs, String italicPrefs, String underlinePrefs) {
        int style = SWT.NORMAL;

        boolean isBold = store.getBoolean(boldPrefs);
        if (isBold) {
            style = style | SWT.BOLD;
        }

        boolean isItalic = store.getBoolean(italicPrefs);
        if (isItalic) {
            style = style | SWT.ITALIC;
        }

        boolean isUnderline = store.getBoolean(underlinePrefs);
        if (isUnderline) {
            style = style | TextAttribute.UNDERLINE;
        }

        RGB color = PreferenceConverter.getColor(store, colorPrefs);
        TextAttribute attr = new TextAttribute(colorManager.getColor(color), null, style);
        return attr;
    }

    @Override
    public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
        // TODO
    }

}
