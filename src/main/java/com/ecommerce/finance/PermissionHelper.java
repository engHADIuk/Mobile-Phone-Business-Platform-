package com.ecommerce.finance;

import javafx.scene.control.Button;

public class PermissionHelper {

    /**
     * Disables edit/modify/delete buttons if the current user is an employee.
     * Call this in every controller after creating those buttons.
     */
    public static void applyReadOnlyPermissions(Button editBtn, Button modifyBtn, Button deleteBtn, Button... extraButtons) {
        boolean isEmployee = SessionManager.getInstance().isEmployee();
        if (isEmployee) {
            editBtn.setDisable(true);
            modifyBtn.setDisable(true);
            deleteBtn.setDisable(true);
            for (Button btn : extraButtons) {
                btn.setDisable(true);
            }
        }
    }
}