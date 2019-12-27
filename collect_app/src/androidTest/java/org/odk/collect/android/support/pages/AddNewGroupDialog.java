package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

public class AddNewGroupDialog extends Page<AddNewGroupDialog> {

    AddNewGroupDialog(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public AddNewGroupDialog assertOnPage() {
        checkIsStringDisplayed(R.string.entering_repeat_ask);
        return this;
    }

    public FormEntryPage clickOnAddGroup(FormEntryPage destination) {
        clickOnString(R.string.add_another);
        return destination;
    }

    public FormEntryPage clickOnDoNotAddGroup(FormEntryPage destination) {
        clickOnString(R.string.add_repeat_no);
        return destination;
    }

}
