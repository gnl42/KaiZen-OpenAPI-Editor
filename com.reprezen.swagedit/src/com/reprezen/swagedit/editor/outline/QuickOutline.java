package com.reprezen.swagedit.editor.outline;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.IShowInTarget;

public class QuickOutline extends PopupDialog
        implements IInformationControl, IInformationControlExtension, IInformationControlExtension2 {

    private TreeViewer treeViewer;
    private Text filterText;

    public QuickOutline(Shell parent, IShowInTarget showInTarget) {
        super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, true, true, true, true, true, null, null);
        create();
    }

    @Override
    protected Control createTitleControl(Composite parent) {
        filterText = new Text(parent, SWT.NONE);
        Dialog.applyDialogFont(filterText);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.CENTER;
        filterText.setLayoutData(data);

        filterText.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    gotoSelectedElement();
                }
                if (e.keyCode == SWT.ARROW_DOWN) {
                    treeViewer.getTree().setFocus();
                }
                if (e.keyCode == SWT.ARROW_UP) {
                    treeViewer.getTree().setFocus();
                }
                if (e.character == SWT.ESC) {
                    QuickOutline.this.close();
                }
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                // refresh viewer to re-filter
                treeViewer.getControl().setRedraw(false);
                treeViewer.refresh();
                treeViewer.expandAll();
                selectFirstMatch();
                treeViewer.getControl().setRedraw(true);
            }
        });
        return filterText;
    }

    protected void selectFirstMatch() {
        final Tree tree = treeViewer.getTree();
        TreeItem element = findElement(tree.getItems());

        if (element != null) {
            tree.setSelection(element);
            tree.showItem(element);
        } else {
            treeViewer.setSelection(StructuredSelection.EMPTY);
        }
    }

    private TreeItem findElement(TreeItem[] items) {
        return findElement(items, null, true);
    }

    private TreeItem findElement(TreeItem[] items, TreeItem[] toBeSkipped, boolean allowToGoUp) {
        // First search at same level
        for (int i = 0; i < items.length; i++) {
            final TreeItem item = items[i];
            OutlineElement element = (OutlineElement) item.getData();
            if (element != null) {
                if (matchesFilter(element))
                    return item;
            }
        }

        // Go one level down for each item
        for (int i = 0; i < items.length; i++) {
            final TreeItem item = items[i];
            TreeItem foundItem = findElement(selectItems(item.getItems(), toBeSkipped), null, false);
            if (foundItem != null)
                return foundItem;
        }

        if (!allowToGoUp || items.length == 0)
            return null;

        // Go one level up (parent is the same for all items)
        TreeItem parentItem = items[0].getParentItem();
        if (parentItem != null)
            return findElement(new TreeItem[] { parentItem }, items, true);

        // Check root elements
        return findElement(selectItems(items[0].getParent().getItems(), items), null, false);
    }

    private TreeItem[] selectItems(TreeItem[] items, TreeItem[] toBeSkipped) {
        if (toBeSkipped == null || toBeSkipped.length == 0)
            return items;

        int j = 0;
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (!canSkip(item, toBeSkipped))
                items[j++] = item;
        }
        if (j == items.length)
            return items;

        TreeItem[] result = new TreeItem[j];
        System.arraycopy(items, 0, result, 0, j);
        return result;
    }

    private boolean canSkip(TreeItem item, TreeItem[] toBeSkipped) {
        if (toBeSkipped == null)
            return false;

        for (int i = 0; i < toBeSkipped.length; i++) {
            if (toBeSkipped[i] == item)
                return true;
        }
        return false;
    }

    protected TreeViewer createTreeViewer(Composite parent) {
        final Tree tree = new Tree(parent, SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = tree.getItemHeight();
        tree.setLayoutData(gd);

        final TreeViewer treeViewer = new TreeViewer(tree);
        treeViewer.setContentProvider(new OutlineContentProvider());
        treeViewer.setLabelProvider(new OutlineStyledLabelProvider());
        treeViewer.addFilter(new NamePatternFilter());
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

        tree.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {

                if (tree.getSelectionCount() < 1)
                    return;

                if (e.button != 1)
                    return;

                if (tree.equals(e.getSource())) {
                    Object o = tree.getItem(new Point(e.x, e.y));
                    TreeItem selection = tree.getSelection()[0];
                    if (selection.equals(o)) {
                        gotoSelectedElement();
                    }
                }
            }
        });

        return treeViewer;
    }

    protected void gotoSelectedElement() {
        ITreeSelection selection = (ITreeSelection) treeViewer.getSelection();
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof OutlineElement) {
            OutlineElement element = (OutlineElement) firstElement;
            // TODO
            // IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            // IRegion region = element.getRegion(editor);

            // if (region != null) {
            // editor.selectAndReveal(region.getOffset(), region.getLength());
            // close();
            // }
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        treeViewer = createTreeViewer(parent);

        final Tree tree = treeViewer.getTree();
        tree.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC) {
                    QuickOutline.this.close();
                }
            }

            public void keyReleased(KeyEvent e) {
                // do nothing
            }
        });

        tree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // do nothing
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // gotoSelectedElement();
            }
        });
        return treeViewer.getControl();
    }

    @Override
    public void setInput(Object input) {
        treeViewer.setInput(input);
    }

    @Override
    public boolean hasContents() {
        if (treeViewer == null || treeViewer.getInput() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void setInformation(String information) {
    }

    @Override
    public void setSizeConstraints(int maxWidth, int maxHeight) {
    }

    @Override
    public Point computeSizeHint() {
        return getShell().getSize();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            open();
        } else {
            saveDialogBounds(getShell());
            getShell().setVisible(false);
        }
    }

    @Override
    public void setSize(int width, int height) {
        getShell().setSize(width, height);
    }

    @Override
    public void setLocation(Point location) {
        getShell().setLocation(location);
    }

    @Override
    public void dispose() {
        close();
    }

    @Override
    public void addDisposeListener(DisposeListener listener) {
        getShell().addDisposeListener(listener);
    }

    @Override
    public void removeDisposeListener(DisposeListener listener) {
        getShell().removeDisposeListener(listener);
    }

    @Override
    public void setForegroundColor(Color foreground) {
        applyForegroundColor(foreground, getContents());
    }

    @Override
    public void setBackgroundColor(Color background) {
        applyBackgroundColor(background, getContents());
    }

    @Override
    public boolean isFocusControl() {
        return treeViewer.getTree().isFocusControl() || filterText.isFocusControl();
    }

    @Override
    public void setFocus() {
        getShell().forceFocus();
        if (filterText != null) {
            filterText.setFocus();
        }
    }

    @Override
    public void addFocusListener(FocusListener listener) {
        getShell().addFocusListener(listener);
    }

    @Override
    public void removeFocusListener(FocusListener listener) {
        getShell().removeFocusListener(listener);
    }

    private boolean matchesFilter(Object element) {
        if (treeViewer == null)
            return true;

        if (element instanceof OutlineElement) {
            String matchName = ((OutlineElement) element).getText();
            String text = filterText.getText();

            if (matchName != null) {
                if (text.startsWith("*")) {
                    return matchName.contains(text.substring(1));
                } else {
                    return matchName.startsWith(text);
                }
            }
        }
        return false;
    }

    public class NamePatternFilter extends ViewerFilter {

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return matchesFilter(element);
        }
    }
}
