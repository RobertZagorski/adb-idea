package com.developerphil.adbidea.ui.permissions;

import com.android.ddmlib.AndroidDebugBridge;
import com.developerphil.adbidea.model.PermissionState;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * Created by Robert on 23.08.2016.
 */
public class PermissionChooser {
    private static final String[] COLUMN_TITLES = new String[]{"Permission", "State"};
    private static final int PERMISSION_NAME_COLUMN_INDEX = 0;
    private static final int PERMISSION_STATE_COLUMN_INDEX = 1;
    private static final int REFRESH_INTERVAL_MS = 500;

    private JComponent mPanel;

    private final AndroidDebugBridge mADBridge;

    private Action mOkAction;

    private CheckBoxList checkBoxList;

    List<PermissionState> initialPermissionStates;

    public PermissionChooser(Project project,
                             @NotNull final Action okAction,
                             List<PermissionState> permissions) {
        mADBridge = AndroidSdkUtils.getDebugBridge(project);
        this.initialPermissionStates = permissions;
        JCheckBox[] checkBoxes = new JCheckBox[permissions.size()];
        checkBoxList = new CheckBoxList(permissions);
        for (int i = 0 ; i < permissions.size() ; ++i) {
            checkBoxes[i] = new JCheckBox(permissions.get(i).getPermissionName());
            checkBoxes[i].setSelected(permissions.get(i).isGranted());
        }
        checkBoxList.addAll(checkBoxes);
        mPanel = ScrollPaneFactory.createScrollPane(checkBoxList);
        mPanel.setPreferredSize(new Dimension(450, 220));
    }

    @Nullable
    public JComponent getPanel() {
        return mPanel;
    }

    public List<PermissionState> getPermissions() {
        List<PermissionState> outputList = new ArrayList<>();
        for (int i = 0 ; i < initialPermissionStates.size() ; ++i) {
            PermissionState initialPermissionState = initialPermissionStates.get(i);
            JCheckBox checkBox = checkBoxList.getCheckBox(i);
            if (checkBox.isSelected() == initialPermissionState.isGranted()) {
                continue;
            }
            PermissionState permissionState = new PermissionState();
            permissionState.setPermissionName(initialPermissionState.getPermissionName());
            permissionState.setGranted(checkBox.isSelected());
            outputList.add(permissionState);
        }
        return outputList;
    }

    public class CheckBoxList extends JList {

        protected Border noFocusBorder =
                new EmptyBorder(1, 1, 1, 1);

        public CheckBoxList(List<PermissionState> permissions)
        {
            setCellRenderer(new PermissionChooser.CheckBoxList.CellRenderer());

            addMouseListener(new MouseAdapter()
                             {
                                 public void mousePressed(MouseEvent e)
                                 {
                                     int index = locationToIndex(e.getPoint());

                                     if (index != -1) {
                                         JCheckBox checkbox = (JCheckBox)
                                                 getModel().getElementAt(index);
                                         checkbox.setSelected(
                                                 !checkbox.isSelected());
                                         repaint();
                                     }
                                 }
                             }
            );

            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }

        public void addCheckbox(JCheckBox checkBox) {
            ListModel currentList = this.getModel();
            JCheckBox[] newList = new JCheckBox[currentList.getSize() + 1];
            for (int i = 0; i < currentList.getSize(); i++) {
                newList[i] = (JCheckBox) currentList.getElementAt(i);
            }
            newList[newList.length - 1] = checkBox;
            setListData(newList);
        }

        public JCheckBox getCheckBox(int position) {
            return (JCheckBox) getModel().getElementAt(position);
        }

        public void addAll(JCheckBox[] checkBoxes) {
            setListData(checkBoxes);
        }

        protected class CellRenderer implements ListCellRenderer
        {
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus)
            {
                JCheckBox checkbox = (JCheckBox) value;
                checkbox.setBackground(isSelected ?
                        getSelectionBackground() : getBackground());
                checkbox.setForeground(isSelected ?
                        getSelectionForeground() : getForeground());
                checkbox.setEnabled(isEnabled());
                checkbox.setFont(getFont());
                checkbox.setFocusPainted(false);
                checkbox.setBorderPainted(true);
                checkbox.setBorder(isSelected ?
                        UIManager.getBorder(
                                "List.focusCellHighlightBorder") : noFocusBorder);
                return checkbox;
            }
        }
    }
}
